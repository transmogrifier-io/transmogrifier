package TransmogrifierJava;

import java.util.HashMap;
import java.util.Map;

public class Filters {
    /**
     * Removes all the nulls in the data
     * @param data
     * @return String data without the nulls
     */
    public static String null_filter(String data) {
        return data;
    }

    /**
     * Converts the data to uppercase
     * @param data
     * @return String data in uppercase
     */
    public static String to_upper(String data) {
        return data.toUpperCase();

    }

    /**
     * Converts the data to lowercase
     * @param data
     * @return String data in lowercase
     */
    public static String to_lower(String data) {
        return data.toLowerCase();
    }

    /**
     * Gets the filter function (null, upper, lower)
     * @param filter
     * @param data
     * @return String filtered data
     */
    public static String getFilters(String filter, String data) {
        Map<String, String> filters = new HashMap<>();

        filters.put("null_filter", null_filter(data));

        filters.put("to_upper", to_upper(data));

        filters.put("to_lower", to_lower(data));
        
        if (filters.isEmpty()) {
            System.out.println("filter not found");
            return data;
            
        }else{
            return filters.get(filter);
        }
    }
}