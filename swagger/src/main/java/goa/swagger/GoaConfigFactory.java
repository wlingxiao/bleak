package goa.swagger;

public class GoaConfigFactory {

    private static GoaSwaggerConfig instance;

    public static void setConfig(GoaSwaggerConfig routes) {
        instance = routes;
    }

    public static GoaSwaggerConfig getConfig() {
        return instance;
    }

}
