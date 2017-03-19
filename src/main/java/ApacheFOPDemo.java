import org.apache.fop.apps.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.Arrays;

public class ApacheFOPDemo {

    public static void main(String[] args) {
        try {
            generatePDFFromXml();
            generatePDFFromJavaObject();
        } catch (FOPException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void generatePDFFromXml()
            throws IOException, FOPException, TransformerException {
        // the XML file which provides the input
        StreamSource xmlStreamSource = new StreamSource(new File("book.xml"));
        generatePDF(xmlStreamSource);
    }

    private static void generatePDFFromJavaObject() throws Exception {
        Books books = new Books();

        Book book1 = getBook("A Time To Kill", 1989);
        Book book2 = getBook("The Film", 1991);
        Book book3 = getBook("The Client", 1993);
        books.setBookList(Arrays.asList(book1, book2, book3));
        books.setAuthor("John Grisham");
        ByteArrayOutputStream xmlSource = getXMLSource(books);
        StreamSource streamSource =
                new StreamSource(new ByteArrayInputStream(xmlSource.toByteArray()));
        generatePDF(streamSource);
    }

    private static Book getBook(String name, int year) {
        Book book = new Book();
        book.setName(name);
        book.setYear(year);
        return book;
    }

    private static ByteArrayOutputStream getXMLSource(Books books) throws Exception {
        JAXBContext context;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            context = JAXBContext.newInstance(Books.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(books, outStream);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return outStream;
    }

    private static void generatePDF(StreamSource streamSource)
            throws FOPException, TransformerException, IOException {
        File xsltFile = new File("template.xsl");

        // create an instance of fop factory
        FopFactory fopFactory = FopFactory.newInstance();
        // a user agent is needed for transformation
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        // Setup output
        OutputStream out = new java.io.FileOutputStream("book.pdf");

        try {
            // Construct fop with desired output format
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

            // Setup XSLT
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(new StreamSource(xsltFile));

            // Resulting SAX events (the generated FO) must be piped through to FOP
            Result res = new SAXResult(fop.getDefaultHandler());

            // Start XSLT transformation and FOP processing
            // That's where the XML is first transformed to XSL-FO and then
            // PDF is created
            transformer.transform(streamSource, res);
        } finally {
            out.close();
        }
    }

}
