package jaina.compiler.dispose;

import com.sun.tools.javac.code.Symbol;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

import javax.annotation.processing.Filer;

/**
 * Created by kuma on 2018/9/15.
 */

public class SAXParserHandler extends DefaultHandler {

    private Filer mFiler;
    private String mPkg;
    private String mXmlName;

    private ArrayList<ViewElement> viewElements = new ArrayList<ViewElement>();
    public ArrayList<ViewElement> getViewElements() {
        return viewElements;
    }

    public SAXParserHandler(Filer filer, String pkg, String xmlName) {
        this.mFiler = filer;
        this.mPkg = pkg;
        this.mXmlName = xmlName;
    }

    /**
     * 解析xml元素
     */
    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if("include".equals(qName)) {
            String value = attributes.getValue("layout");
            String xmlName = getLayoutName(value);
            viewElements.addAll(ResourceHelper.genXmlRes(mFiler, mPkg, xmlName));
        }else if (attributes.getValue("android:id") != null) {
            ViewElement viewElement = new ViewElement(attributes.getValue("android:id"), qName);
            viewElements.add(viewElement);
        }

    }

    public static String getLayoutName(String layout) {
        if (layout == null || !layout.startsWith("@") || !layout.contains("/")) {
            return null;
        }
        String[] parts = layout.split("/");
        if (parts.length != 2) {
            return null;
        }
        return parts[1];
    }

}