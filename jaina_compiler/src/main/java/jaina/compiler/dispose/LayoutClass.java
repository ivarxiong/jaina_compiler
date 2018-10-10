package jaina.compiler.dispose;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.code.Symbol;

import java.util.ArrayList;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;

/**
 * Created by JokAr on 16/8/8.
 */
public class LayoutClass {

    private String packageName;
    private Symbol.ClassSymbol mClassSymbol;
    private ArrayList<ViewElement> viewElements;
    private Elements mElements;
    private String className;

    public LayoutClass(Symbol.ClassSymbol classSymbol, String className, Elements elements,String name,  ArrayList<ViewElement> viewElements) {
        mClassSymbol = classSymbol;
        mElements = elements;
        this.viewElements = viewElements;
        packageName = name;
        this.className = className;
    }

    public JavaFile generateFile() {
        MethodSpec.Builder injectMethod = MethodSpec.methodBuilder("bindView")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(TypeName.get(mClassSymbol.asType()), "host", Modifier.FINAL)
                .addParameter(TypeName.OBJECT, "source")
                .addParameter(TypeUtil.Finder, "finder")
                ;

        for(ViewElement viewElement: viewElements) {
            injectMethod.addStatement("host.$N = ($N)(finder.findView(source, $N))",
                    viewElement.getFieldName(), viewElement.getFieldType(),
                    ("".endsWith(packageName) ? "" : (packageName + ".")) + "R.id." + viewElement.getId());
        }

        TypeSpec injectClass = TypeSpec.classBuilder(className + "$$ViewInject")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(TypeUtil.Bind, TypeName.get(mClassSymbol.asType())))
                .addMethod(injectMethod.build())
                .build();
        String packgeName = mElements.getPackageOf(mClassSymbol).getQualifiedName().toString();

        return JavaFile.builder(packgeName, injectClass).build();
    }
}
