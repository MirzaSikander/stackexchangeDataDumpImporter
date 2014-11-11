package DataManagement;

/**
* This is a tool for importing StackOverflow data dump into MySQL database.
* @author ptantiku
*/
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class Main {

 Properties properties = null;
 String serveraddress;
 String port;
 String username;
 String password;
 String databasename;
 private int rowspercommit;
 String filePath;
 String site = "";
 Connection connection;


 /**
  * Main Program
  * @param args
  * @throws IOException
  */
 public static void main(String[] args) throws IOException {
  Main xmlImporter = new Main();
  xmlImporter.importXMLFolder();
  xmlImporter.closeDB();
 }

 public Main() {
  loadConfig();
  connectDB();
 }

 private void loadConfig() {
  // create an instance of properties class
  properties = new Properties();

  // try retrieve data from file
  try {
   properties.load(new FileInputStream("config.properties"));
   serveraddress = properties.getProperty("serveraddress");
   port = properties.getProperty("port");
   username = properties.getProperty("username");
   password = properties.getProperty("password");
   databasename = properties.getProperty("databasename") + site;
   rowspercommit = Integer.parseInt(properties.getProperty("rowspercommit"));
   filePath = properties.getProperty("path") + site;

  } // catch exception in case properties file does not exist
  catch (IOException e) {
   e.printStackTrace();
  }
 }

 private void connectDB() {
  // connect to MySQL
  connection = null;
  try {
   // Load the JDBC driver
   String driverName = "org.gjt.mm.mysql.Driver"; // MySQL MM JDBC
   // driver
   Class.forName(driverName);

   // Create a connection to the database
   String url = "jdbc:mysql://localhost:" + port + "/" + databasename;
   connection = DriverManager.getConnection(url, username, password);
   connection.setAutoCommit(false);  //for batch insert
  } catch (ClassNotFoundException e) {
   // Could not find the database driver
   e.printStackTrace();
  } catch (SQLException e) {
   // Could not connect to the database
   e.printStackTrace();
  }
 }

 public void importXMLFolder() throws IOException {
  File path = new File(filePath);
  if (path == null || !path.isDirectory()) {
   throw new IOException("Path is wrong");
  }
  String fileList = properties.getProperty("filelist");
  String[] fileListArray = fileList.split(",");

  String tableName = properties.getProperty("tablename");
  String[] tableNameArray = tableName.split(",");

  for(int i=0;i<fileListArray.length;i++){
  //for (int i = 0; i < 1; i++) {
   String filename = filePath + File.separatorChar + fileListArray[i];
   String table = tableNameArray[i];
   importXMLFile(filename, table);
  }
 }

 public void importXMLFile(String filename, String tablename)
   throws IOException {
  System.out.println("Processing file: " + filename);

  String fields = properties.getProperty(tablename+"_fields");
  String[] fieldsArray = fields.split(",");



  try {
   FileInputStream fin = new FileInputStream(new File(filename));
   XMLInputFactory factory = XMLInputFactory.newInstance();
   XMLStreamReader r = factory.createXMLStreamReader(fin);
   int attributeCount = 0;
   int rowCount = 0;

   //connection part
   if(connection==null||connection.isClosed())
    connectDB();
   //REPLACE is non-standard SQL from MySQL, it's counter part of INSERT IGNORE
   String sql = "REPLACE INTO "+tablename+" ("+fields+") VALUES ("+fields.replaceAll("[^,]+", "?")+")";
   System.out.println("SQL: "+sql);
   PreparedStatement pstmt = connection.prepareStatement(sql);

   // start parsing
   int event = r.getEventType();
   while (true) {
    // for each wanted element
    if (event == XMLStreamConstants.START_ELEMENT
      && r.getName().toString().equalsIgnoreCase("row")) {

     System.out.println("Table : "+tablename+" / Row : "+rowCount );
     if (attributeCount == 0)
      attributeCount = r.getAttributeCount();

     for (int a = 0; a < attributeCount; a++) {
      System.out.print("\t{" + r.getAttributeName(a) + ":"
        + r.getAttributeValue(a) + "}");
     }
     System.out.println();

     //put each parameter to SQL
     int f=1;
     for (String field : fieldsArray){
      pstmt.setString(f++,r.getAttributeValue(null, field));
     }
     pstmt.addBatch();
     rowCount++;

     if(rowCount%rowspercommit==0){
      System.out.println("Importing at row "+rowCount+" ... ");
      pstmt.executeBatch();
      connection.commit();
     }
    } // end for each row.

    // proceeding to next element
    if (!r.hasNext())
     break;
    else
     event = r.next();
   }

   System.out.println("Importing all rows into database (it might take time) ... ");
   pstmt.executeBatch();
   connection.commit();
   pstmt.close();
   r.close();
   fin.close();
   System.out.println("Importing "+filename+" is done");
  } catch (XMLStreamException e) {
   e.printStackTrace();
  } catch (SQLException e) {
   e.printStackTrace();
  }
 }

 public void closeDB(){
  try {
   if(connection!=null&&!connection.isClosed())
    connection.close();
  } catch (SQLException e) {
   e.printStackTrace();
  }
 }
}
