package jaina.compiler.dispose;

import com.sun.tools.javac.code.Symbol;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import jaina.annotation.Layout;

/**
 * Created by kuma on 2018/9/15.
 */

public class ResourceHelper {

    public static File doWithOriAndPrintWriter(Filer filer, JavaFileManager.Location location, String relativePath, String filename){
        try {
            FileObject resource = filer.getResource(location, relativePath, filename);
            System.out.println(" resource = " + resource.toUri());
            String path = resource.toUri().getPath();
            int index = path.lastIndexOf("build");
            String projectPath = path.substring(0, index);
            String xmlPath = projectPath + "src/main/res/layout/" + filename;
            File file = new File(xmlPath);
            if(file.exists() && file.canRead()) return file;
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public static ArrayList<ViewElement> genXmlRes(Filer filer, String pkg, String xmlName) {
        File xmlFile = ResourceHelper.doWithOriAndPrintWriter(filer, StandardLocation.CLASS_OUTPUT, pkg, xmlName + ".xml");
        if(xmlFile != null) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = null;
            try {
                parser = factory.newSAXParser();
                SAXParserHandler handler = new SAXParserHandler(filer, pkg, xmlName);
                parser.parse(xmlFile.getAbsolutePath(), handler);
                ArrayList<ViewElement> viewElements = handler.getViewElements();
                return viewElements;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
