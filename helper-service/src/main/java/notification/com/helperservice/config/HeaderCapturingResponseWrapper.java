package notification.com.helperservice.config;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class HeaderCapturingResponseWrapper extends HttpServletResponseWrapper {

    private final Map<String, String> headers = new HashMap<>();

    public HeaderCapturingResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void setHeader(String name, String value) {
        headers.put(name, value);
        super.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        headers.put(name, value);
        super.addHeader(name, value);
    }

    @Override
    public String getHeader(String name) {
        String value = headers.get(name);
        if (value != null) {
            return value;
        }
        return super.getHeader(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }
}
