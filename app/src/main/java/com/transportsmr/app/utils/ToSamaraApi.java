package com.transportsmr.app.utils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.transportsmr.app.model.Route;
import com.transportsmr.app.model.Stop;
import com.transportsmr.app.model.Transport;
import okhttp3.ResponseBody;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by kirill on 13.01.17.
 */
public interface ToSamaraApi {
    @GET("/api/json")
    @Json
    Call<ArrivalResponse> getArrival(@Query("method") String method,
                                     @Query("KS_ID") String id,
                                     @Query("COUNT") int count,
                                     @Query("os") String os,
                                     @Query("clientid") String clientId,
                                     @Query("authkey") String authKey);

    @GET("/api/classifiers")
    @Xml
    Call<ClassifiersResponse> getClassifiersUpdate();

    @GET("/api/classifiers/routes.xml")
    @Xml
    Call<RoutesResponse> getRoutes();

    @GET("/api/classifiers/stopsFullDB.xml")
    @Xml
    Call<StopsResponse> getStops();


    class ArrivalResponse {
        @SerializedName("arrival")
        @Expose
        private List<Transport> arrival = null;

        public List<Transport> getArrival() {
            return arrival;
        }
    }

    @Root(name = "stops", strict = false)
    class StopsResponse {
        @ElementList(inline = true)
        private List<Stop> stops = null;

        public List<Stop> getStops() {
            return stops;
        }
    }

    @Root(name = "routes", strict = false)
    class RoutesResponse {
        @ElementList(inline = true)
        private List<Route> routes = null;

        public List<Route> getRoutes() {
            return routes;
        }
    }

    @Root(name = "classifiers", strict = false)
    class ClassifiersResponse {
        @ElementList(inline = true)
        private List<File> files = null;

        public List<File> getFiles() {
            return files;
        }
    }

    @Root(name = "file", strict = false)
    class File {
        @Element(name = "modified")
        String modified;

        @Attribute(name = "name")
        String name;

        public String getModified() {
            return modified;
        }

        public String getName() {
            return name;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Json {
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Xml {
    }

    class RetrofitUniversalConverter extends Converter.Factory {

        private final Converter.Factory xml;
        private final Converter.Factory json;

        public RetrofitUniversalConverter() {
            xml = SimpleXmlConverterFactory.create();
            json = GsonConverterFactory.create();
        }

        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {

            for (Annotation annotation : annotations) {

                if (annotation.annotationType() == Xml.class) {
                    return xml.responseBodyConverter(type, annotations, retrofit);
                }

                if (annotation.annotationType() == Json.class) {
                    return json.responseBodyConverter(type, annotations, retrofit);
                }

            }

            return null;
        }
    }
}
