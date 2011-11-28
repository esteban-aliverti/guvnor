package org.drools.guvnor.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class DecoratedTextArea extends Composite {

    interface DecoratedTextAreaBinder
        extends
        UiBinder<Widget, DecoratedTextArea> {
    }

    private static DecoratedTextAreaBinder uiBinder = GWT.create( DecoratedTextAreaBinder.class );

    @UiField
    TextArea                               textArea;

    public DecoratedTextArea() {

        initWidget( uiBinder.createAndBindUi( this ) );
    }

    public void setVisibleLines(int index) {
        textArea.setVisibleLines( index );
    }

    public void setText(String text) {
        textArea.setText( text );
    }

    public void addChangeHandler(ChangeHandler changeHandler) {
        textArea.addChangeHandler( changeHandler );
    }

    public String getText() {
        return textArea.getText();
    }
    
    public void setEnabled(boolean enabled) {
    	textArea.setEnabled(enabled);
    }  

}
