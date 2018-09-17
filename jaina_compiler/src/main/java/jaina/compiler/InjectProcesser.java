package jaina.compiler;

import com.google.auto.service.AutoService;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import jaina.annotation.Layout;
import jaina.compiler.dispose.LayoutClass;
import jaina.compiler.dispose.ResourceHelper;
import jaina.compiler.dispose.SAXParserHandler;
import jaina.compiler.dispose.ViewElement;

/**
 * Created by ivar on 2018/9/15.
 */

@AutoService(Processor.class)
@SupportedAnnotationTypes({"jaina.annotation.Layout"})
public class InjectProcesser extends AbstractProcessor {

    private Filer mFiler; //文件相关的辅助类
    private Elements mElementUtils; //元素相关的辅助类
    private Messager mMessager; //日志相关的辅助类

    private Trees trees;
    private TreeMaker treeMaker;
    private Name.Table names;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        System.out.println(" InjectProcesser init ");
        super.init(processingEnv);
        mFiler = processingEnv.getFiler();
        mElementUtils = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
        trees = Trees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        treeMaker = TreeMaker.instance(context);
        names = Names.instance(context).table;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            processViews(roundEnv);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            error(e.getMessage());
        }
        return true;
    }

    /**
     * 指定使用的 Java 版本。通常返回 SourceVersion.latestSupported()。
     * @return
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    private void processViews(RoundEnvironment roundEnv) {

        for (final Element element : roundEnv.getElementsAnnotatedWith(Layout.class)) {
            System.out.println("  Layout ============================  ");
            // 只处理作用在类上的注解
            if (element.getKind() == ElementKind.CLASS) {
                System.out.println("  class name ============================  " + element.getSimpleName());
                final ArrayList<ViewElement> viewElements = handle(element);
                if(viewElements == null || viewElements.isEmpty()) return;
                final JCTree tree = (JCTree) trees.getTree(element);
                tree.accept(new TreeTranslator() {
                    @Override
                    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                        String classSimpleName = jcClassDecl.getSimpleName().toString();
                        String elementSimpleName = element.getSimpleName().toString();
                        if(classSimpleName == null || elementSimpleName == null) return;
                        boolean isClass = (classSimpleName.equals(elementSimpleName));
                        if(isClass) {
                            System.out.println(" ===== JCClassDecl is same  ====  ");
                            jcClassDecl.mods = (JCTree.JCModifiers) this.translate((JCTree) jcClassDecl.mods);
                            jcClassDecl.typarams = this.translateTypeParams(jcClassDecl.typarams);
                            jcClassDecl.extending = (JCTree.JCExpression) this.translate((JCTree) jcClassDecl.extending);
                            jcClassDecl.implementing = this.translate(jcClassDecl.implementing);
                            ListBuffer<JCTree> statements = new ListBuffer<>();
                            List<JCTree> oldList = this.translate(jcClassDecl.defs);
                            for (JCTree jcTree : oldList) {
                                statements.append(jcTree);
                            }
                            for(ViewElement viewElement : viewElements) {
                                String packageDots[] = viewElement.getFieldType().split("\\.");
                                if(packageDots == null || packageDots.length < 2) continue;
                                String[] viewDots = new String[packageDots.length - 2];
                                for (int j = 0; j < viewDots.length; j++) {
                                    viewDots[j] = packageDots[j + 2];
                                }
                                JCTree.JCExpression logType = chainDots(packageDots[0], packageDots[1], viewDots);
                                JCTree.JCVariableDecl fieldDecl = treeMaker.VarDef(
                                        treeMaker.Modifiers(0L, List.<JCTree.JCAnnotation>nil()),
                                        names.fromString(viewElement.getFieldName()),
                                        logType,
                                        null
                                );
                                statements.append(fieldDecl);
                            }
                            jcClassDecl.defs = statements.toList(); //更新
                            this.result = jcClassDecl;
                        }else {
                            super.visitClassDef(jcClassDecl);
                        }
                    }
                });
                Symbol.ClassSymbol classSymbol = (Symbol.ClassSymbol) element;
                LayoutClass layoutClass = new LayoutClass(classSymbol, mElementUtils, classSymbol.getAnnotation(Layout.class).packageName(), viewElements);
                try {
                    layoutClass.generateFile().writeTo(mFiler);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public ArrayList<ViewElement> handle(Element element) {
        if (!(element.getKind() == ElementKind.CLASS)) return null;
        Symbol.ClassSymbol classSymbol = (Symbol.ClassSymbol) element;
        String qualifiedName = classSymbol.getQualifiedName().toString();
        String clz = classSymbol.getSimpleName().toString();
        String pkg = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
        return ResourceHelper.genXmlRes(mFiler, pkg, classSymbol.getAnnotation(Layout.class).value());
    }

    public JCTree.JCExpression chainDots(String elem1, String elem2, String... elems) {
        assert elems != null;
        TreeMaker maker = treeMaker;
        JCTree.JCExpression e = null;
        if (elem1 != null) e = maker.Ident(names.fromString(elem1));
        if (elem2 != null) e = e == null ? maker.Ident(names.fromString(elem2)) : maker.Select(e, names.fromString(elem2));
        for (int i = 0 ; i < elems.length ; i++) {
            e = e == null ? maker.Ident(names.fromString(elems[i])) : maker.Select(e, names.fromString(elems[i]));
        }
        assert e != null;
        return e;
    }

    private void error(String msg, Object... args) {
        mMessager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args));
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return super.getSupportedAnnotationTypes();
    }

}
