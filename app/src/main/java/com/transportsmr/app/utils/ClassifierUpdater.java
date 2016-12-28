package com.transportsmr.app.utils;

import com.transportsmr.app.model.DaoSession;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

/**
 * Created by kirill on 28.12.16.
 */
public abstract class ClassifierUpdater<Model> {

    private final String classifierUrl;
    private final DaoSession daoSession;
    private final String fileName;

    public ClassifierUpdater(DaoSession daoSession, String fileName, String classifierUrl) {
        this.daoSession = daoSession;
        this.fileName = fileName;
        this.classifierUrl = classifierUrl;
    }

    public String getClassifierUrl() {
        return classifierUrl;
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean update(XmlPullParser xmlPullParser) {
        try {
            update(parse(xmlPullParser));
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    protected abstract List<Model> parse(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException;

    protected abstract void update(List<Model> list);
}
