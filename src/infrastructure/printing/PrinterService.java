package infrastructure.printing;

import java.security.SecureRandom;
import javax.print.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class PrinterService {

    public static String generateRandomCode() {
        String ALLOWED_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int length = 8;

        SecureRandom random = new SecureRandom();
        StringBuilder randomCode = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(ALLOWED_CHARACTERS.length());
            randomCode.append(ALLOWED_CHARACTERS.charAt(randomIndex));
        }

        return randomCode.toString();
    }

    public static String printCode_39(String barcodeId, int copies, boolean isEvenLabel) {
        String xPrinterName = "Xprinter XP-246B";

        String tsplCommand = "SIZE 40 mm, 25 mm\r\n" +  // Label size
                "GAP 2 mm, 0 mm\r\n" +                  // Gap between labels
                "DIRECTION 1\r\n" +                     // Print orientation
                "CLS\r\n";                              // Clear the image buffer
        if (isEvenLabel) {
            tsplCommand +=
                    "BARCODE 20,16,\"39\",50,1,0,2,4,\"" + barcodeId + "\"\r\n" +
                            "BARCODE 20,116,\"39\",50,1,0,2,4,\"" + barcodeId + "\"\r\n" +
                            "PRINT " + copies + "\r\n";
        } else {
            tsplCommand +=
                    "BARCODE 20,16,\"39\",50,1,0,2,4,\"" + barcodeId + "\"\r\n" +
                            "PRINT " + copies + "\r\n";
        }

        return sendRawToPrinter(xPrinterName, tsplCommand);
    }

    private static String sendRawToPrinter(String printerName, String command) {
        // Find all available print services
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        PrintService myPrinter = null;

        // Search for the Xprinter by name
        for (PrintService service : services) {
            if (service.getName().equalsIgnoreCase(printerName)) {
                myPrinter = service;
                break;
            }
        }

        if (myPrinter == null)
            return "Printer '" + printerName + "' not found. Check your OS printer settings.";

        try {
            // Convert the TSPL string into raw bytes
            byte[] commandBytes = command.getBytes(StandardCharsets.US_ASCII);
            InputStream inputStream = new ByteArrayInputStream(commandBytes);

            // Tell Java we are sending raw bytes, not a document or image
            DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
            Doc doc = new SimpleDoc(inputStream, flavor, null);

            // Create the print job and send it
            DocPrintJob job = myPrinter.createPrintJob();
            job.print(doc, null);

            return "TSPL command sent successfully to " + myPrinter.getName();

        } catch (PrintException e) {
            org.slf4j.LoggerFactory.getLogger(PrinterService.class).error("Failed to send print job: {}", e.getMessage());
            return "Failed to send print job: " + e.getMessage();
        }
    }

}

