package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Utility class for pretty-printing objects using Gson.
 */
public class Print {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Pretty prints any object to the console along with its class name.
     *
     * @param object the object to be printed
     * @param <T>    the type of the object
     */
    public static <T> void prettyPrint(T object) {
        if (object != null) {
            try {
                String className = object.getClass().getName();
                String json = gson.toJson(object);
                System.out.println("Class Name: " + className);
                System.out.println("Object Details:\n" + json);
            } catch (Exception e) {
                System.err.println("Error while printing object: " + e.getMessage());
            }
        } else {
            System.out.println("Object is null");
        }
    }

    /**
     * Returns a pretty-printed JSON string of the object, including the class name.
     *
     * @param object the object to be printed
     * @param <T>    the type of the object
     * @return formatted string or error message
     */
    public static <T> String getPrettyPrintedString(T object) {
        if (object != null) {
            try {
                String className = object.getClass().getName();
                String json = gson.toJson(object);
                return "Class Name: " + className + "\nObject Details:\n" + json;
            } catch (Exception e) {
                return "Error while printing object: " + e.getMessage();
            }
        } else {
            return "Object is null";
        }
    }
}
