package com.transportsmr.daogen;


import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Property;
import org.greenrobot.greendao.generator.Schema;

/**
 * Created by kirill on 26.11.2016.
 */
public class GreenDaoGenerator {
    public static void main(String[] args) {
        Schema schema = new Schema(1, "com.transportsmr.app.model");

        Entity stop = schema.addEntity("Stop");
        stop.addIdProperty();
        stop.addStringProperty("ks_id");
        stop.addStringProperty("title");
        stop.addStringProperty("title_lc");
        stop.addStringProperty("adjacentStreet");
        stop.addStringProperty("adjacentStreet_lc");
        stop.addStringProperty("direction");
        stop.addStringProperty("cluster");
        stop.addStringProperty("busesMunicipal");
        stop.addStringProperty("busesCommercial");
        stop.addStringProperty("busesPrigorod");
        stop.addStringProperty("busesSeason");
        stop.addStringProperty("busesSpecial");
        stop.addStringProperty("trams");
        stop.addStringProperty("trolleybuses");
        stop.addStringProperty("metros");
        stop.addStringProperty("infotabloExists");
        stop.addFloatProperty("latitude");
        stop.addFloatProperty("longitude");
        stop.addBooleanProperty("favorite");

        /*Entity favorite = schema.addEntity("Favorite");
        favorite.addIdProperty();
        Property ksId = favorite.addLongProperty("stopId").getProperty();
        favorite.addToOne(stop, ksId);*/

        try {
            (new DaoGenerator()).generateAll(schema, "./app/src/main/java");//"../app/src/main/java");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
