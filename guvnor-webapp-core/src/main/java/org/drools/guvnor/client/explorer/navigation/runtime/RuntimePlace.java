package org.drools.guvnor.client.explorer.navigation.runtime;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class RuntimePlace extends Place {

    private final int id;

    public RuntimePlace(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        RuntimePlace that = (RuntimePlace) o;

        if ( id != that.id ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public static class Tokenizer implements PlaceTokenizer<RuntimePlace> {

        private final String PLACE_ID = "RUNTIME=";

        public String getToken(RuntimePlace place) {
            return PLACE_ID + place.getId();
        }

        public RuntimePlace getPlace(String token) {
            return new RuntimePlace( new Integer( token.substring( PLACE_ID.length() ) ) );
        }
    }
}
