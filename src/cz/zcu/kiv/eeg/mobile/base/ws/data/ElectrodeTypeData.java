package cz.zcu.kiv.eeg.mobile.base.ws.data;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Data container for XML marshaling of electrode type data.
 *
 * @author Petr Miko
 */
@Root(name = "electrodeTypeData")
public class ElectrodeTypeData {

    @Element
    private int id;
    @Element
    private String title;
    @Element(required = false)
    private String description;
    @Element
    private int defaultNumber;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDefaultNumber() {
        return defaultNumber;
    }

    public void setDefaultNumber(int defaultNumber) {
        this.defaultNumber = defaultNumber;
    }
}