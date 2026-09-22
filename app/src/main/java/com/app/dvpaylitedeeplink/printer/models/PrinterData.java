package com.app.dvpaylitedeeplink.printer.models;

import com.denovo.app.invokekozen.printer.enums.PrinterApplicationType;
import com.denovo.app.invokekozen.printer.interfaces.IPrintElementData;
import com.denovo.app.invokekozen.printer.interfaces.IPrintableData;

import java.util.List;

public class PrinterData implements IPrintableData {

    private final List<IPrintElementData> printItems;
    private PrinterApplicationType applicationType;

    public PrinterData(List<IPrintElementData> elements) {
        this.printItems = elements;
    }

    @Override
    public List<IPrintElementData> getPrintItems() {
        return printItems;
    }

    public PrinterApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(PrinterApplicationType applicationType) {
        this.applicationType = applicationType;
    }
}
