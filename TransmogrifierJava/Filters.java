package TransmogrifierJava;

import java.util.HashMap;
import java.util.Map;

public class Filters {
    public static String null_filter(String data) {
        return data;
    }

    public static String to_upper(String data) {
        return data.toUpperCase();

    }

    public static String to_lower(String data) {
        return data.toLowerCase();
    }

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