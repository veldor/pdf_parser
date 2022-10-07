package net.veldor.pdf_parser.model.selection;

import com.google.gson.annotations.Expose;

import java.io.File;

public class Conclusion {
    public File file;
    @Expose
    public int conclusionId;
    @Expose
    public String executionNumber;
    @Expose
    public String executionDate;
    @Expose
    public String diagnostician;
    public String conclusionText;
    @Expose
    public String patientName;
    @Expose
    public String patientSex;
    @Expose
    public String patientBirthdate;
    @Expose
    public String executionArea;
    @Expose
    public String contrastInfo;

    public String filePath;

    @Expose
    public String hash;
}
