
package edu.buffalo.cse.cse486586.simpledynamo;

        import java.io.BufferedReader;
        import java.io.BufferedWriter;
        import java.io.FileInputStream;
        import java.io.FileNotFoundException;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.InputStreamReader;
        import java.io.OutputStream;
        import java.io.OutputStreamWriter;
        import java.io.PrintWriter;
        import java.net.InetAddress;
        import java.net.ServerSocket;
        import java.net.Socket;
        import java.net.UnknownHostException;
        import java.security.MessageDigest;
        import java.security.NoSuchAlgorithmException;
        import java.util.ArrayList;
        import java.util.Arrays;
        import java.util.Date;
        import java.util.Formatter;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;

        import android.content.ContentProvider;
        import android.content.ContentValues;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.MatrixCursor;
        import android.net.Uri;
        import android.os.AsyncTask;
        import android.telephony.TelephonyManager;
        import android.test.suitebuilder.annotation.LargeTest;
        import android.util.Log;

public class SimpleDynamoProvider extends ContentProvider {

    final int MINE = 1;
    final int NOT_MINE = 0;

    final String REPLICA = "REPLICA";
    final String REPLY = "REPLY";
    final String BULK = "BULK";
    final String SEEK = "SEEK";
    final String RESPONSE = "RESPONSE";
    final String DELETE = "DELETE";

    static boolean conditionChanges = false;

    static int[] portListCheck = new int[5];

    static String homePort;


    static String[] allKeySet = new String[200];
    static String[] allValueSet = new String[200];

    static String[] keyValForSingle = new String[2];

    static int[] portList = {5562,5556,5554,5558,5560};

    static int j=0,k= 0;

    final int SERVER_PORT = 10000;

    static int[] arrivalStatus = new int[3];


    static String latestTime = "AAAAAAAAAAA";
    static String tempKey;
    static String tempVal;



    Map<String, String> forQueryKey = new HashMap<String, String>();
    Map<String, String> forQueryVal = new HashMap<String, String>();
    Map<String, List<Integer>> forQueryArrivals = new HashMap<String, List<Integer>>();



    HostInfo hostNode = new HostInfo();

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        int decision = 0;
        try {
            decision = isItMyHash(selection);
            switch (decision){
                case MINE:      deleteTheKeyInCurrentNode(selection);
                    break;

                case NOT_MINE:  deleteTheKeyInOtherNode(selection);
                    break;
                default:    Log.e("Delete","Error in deciding the key");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }finally {
            return 0;
        }
    }

    private void deleteTheKeyInOtherNode(String selection) throws NoSuchAlgorithmException {
        int index = estimateTheRecipient(selection);
        int portOfTheRecipient, portOfSuccessor1, portOfSuccessor2;
        String deleteTheKey = null;

        portOfTheRecipient = portList[0];
        portOfSuccessor1 = portList[(index+1)%5];
        portOfSuccessor2 = portList[(index+2)%5];

        deleteTheKey = DELETE+"<>"+selection;
        Log.i("Delete O ",selection);
        sendToTheNode(deleteTheKey, portOfTheRecipient);
        sendToTheNode(deleteTheKey,portOfSuccessor1);
        sendToTheNode(deleteTheKey,portOfSuccessor2);

    }

    private void deleteTheKeyInCurrentNode(String selection) {
        String[] listOfFiles = new String[100];
        listOfFiles=getContext().fileList();
        int index = -1,successorPort1, successorPort2;
        String deleteTheKey = null;

        Log.i("Delete Single ",selection);
        boolean fileDeletedOrNot = false;
        for(String fName: listOfFiles) {
            fileDeletedOrNot = getContext().deleteFile(fName);
            /*if (fName.equals(selection)) {
                fileDeletedOrNot = getContext().deleteFile(selection);

            }*/
        }
        if (!fileDeletedOrNot) {
            Log.e("Delete", "File Does not exist");
        } else {
            //Delete the entries from the replicas as well
            for (int i = 0; i < portList.length; i++) {
                if (portList[i] == Integer.parseInt(homePort)) {
                    index = i;
                    break;
                }
            }

            successorPort1 = portList[(index + 1) % 5];
            successorPort2 = portList[(index + 2) % 5];

            deleteTheKey = DELETE + "<>" + selection;

            sendToTheNode(deleteTheKey, successorPort1);
            sendToTheNode(deleteTheKey, successorPort2);
            Log.i("Deleted The Entire", selection);
        }

    }
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String keyFile = values.getAsString("key");

        String contentFile = values.getAsString("value");
        Log.v("INSERT KEY&VAL",keyFile+"^&^"+contentFile);
        try {
            int decision = isItMyHash(keyFile);
            switch (decision){
                case MINE:      Log.v("IsItMy -MINE",keyFile);
                    myCourse(keyFile,contentFile);
                    //Log.v("ISITMY","YES");
                    break;
                case NOT_MINE:  Log.v("IsItMy -NOT MINE",keyFile);
                    otherCourse(keyFile,contentFile);
                    //Log.v("ISITMY","NO ");
                    break;
                default: Log.e("INSERT","hash decision Problem");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return uri;
    }

    /*
     * This will estimate the key recepient and send it
     * the insert ones.
     * This will push the insert operations to the other
     * two neighbors.
     * @author: Srinvasan Rajappa
     */
    private void otherCourse(String keyFile,String contentFile) throws NoSuchAlgorithmException {
        //Log.i("otherCourse","--Begin");
        //Log.v("otherCourse",keyFile+"%%%%"+contentFile);

        String hashOfKeyFile = genHash(keyFile);
        int index=0,portOfRecipient, portOfSuccessor1, portOfSuccessor2;
        String replicaOfInsert = REPLICA+"#"+keyFile+"#"+contentFile;


        index = estimateTheRecipient(hashOfKeyFile);
        //Log.i("otherCourse index-", String.valueOf(index));
        portOfRecipient = portList[index];
        portOfSuccessor1 = portList[(index+1)%5];
        portOfSuccessor2 = portList[(index+2)%5];
        Log.v("OtherCourse Sending to",String.valueOf(portOfRecipient));

        sendToTheNode(replicaOfInsert, portOfRecipient);
        sendToTheNode(replicaOfInsert,portOfSuccessor1);
        sendToTheNode(replicaOfInsert,portOfSuccessor2);
    }
    /*
     * Estimate the node to which the
     * key belongs and must be saved to.
     * Returns the index to the port list.
     * @author: Srinivasan Rajappa
     */
    private int estimateTheRecipient(String hashOfKeyFile) throws NoSuchAlgorithmException {
        //Log.v("estimateRece","hash"+hashOfKeyFile);
        for(int i = 0 ; i < portList.length; i++){
            String portNo = portList[i] + "";
            if(i==0){
                if(hashOfKeyFile.compareTo(genHash(portNo)) < 0){
                    return 0;
                }
            }else if(i==(portList.length-1)){
                String prevPortNo = portList[i-1] + "";
                if(hashOfKeyFile.compareTo(genHash(portNo))>0){
                    return 0;
                }
                if(hashOfKeyFile.compareTo(genHash(prevPortNo))>0
                        &&
                        hashOfKeyFile.compareTo(genHash(portNo))<=0){
                    return portList.length-1;
                }

            }else{
                String prevPortNo = portList[i-1] + "";
                if( hashOfKeyFile.compareTo(genHash(prevPortNo)) > 0
                        &&
                        hashOfKeyFile.compareTo(genHash(portNo)) <=0 ){
                    return  i;
                }
            }
        }

        return -1;
    }

    /*
     * It will insert the key value pair in itself and
     * this will push the insert operations to the other
     * two neighbors.
     * @author: Srinivasan Rajappa
     */
    private void myCourse(String keyFile,String contentFile) throws IOException {

        //Insert into self
        Log.i("myCourse --begin",keyFile);

        int index = 0;
        int portOfSuccessor1,portOfSuccessor2;
        String replicaOfInsert;
        FileOutputStream fpOutput = getContext().openFileOutput(keyFile, Context.MODE_PRIVATE);
        byte[] bitVal = contentFile.getBytes();
        fpOutput.write(bitVal);
        fpOutput.close();
        fpOutput.flush();
        Log.v("myCourse done inserting",keyFile+"%%%%"+contentFile);
        //Send to successive neighbors.
        //find the successors
        for(int i = 0 ;i < portList.length;i++){
            if(Integer.parseInt(homePort) == portList[i]){
                index = i;
            }
        }
        portOfSuccessor1 = portList[(index+1) % 5];
        portOfSuccessor2 = portList[(index+2) % 5];
        replicaOfInsert = REPLICA+"#"+keyFile+"#"+contentFile;

        sendToTheNode(replicaOfInsert,portOfSuccessor1);
        sendToTheNode(replicaOfInsert,portOfSuccessor2);
    }
    /*
     * Sends the package to the other side.
     * @author: Srinivasan Rajappa
     */
    private void sendToTheNode(String replicaOfInsert, int portOfOtherNode) {
        portOfOtherNode = 2*portOfOtherNode;
        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, replicaOfInsert, String.valueOf(portOfOtherNode));
    }

    /*
     * Estimate whether the key belongs to the range
     * of host node.
     * @author: Srinivasan Rajappa
     */
    private int isItMyHash(String keyFile) throws NoSuchAlgorithmException {

        Log.i("IsItMyHash","--Begin "+keyFile);

        String hashOfKeyFile = genHash(keyFile);
        //Log.v("IsItMyHash","HASH OF KEY "+hashOfKeyFile);
        String hashOfHomePort = genHash(hostNode.hostPort);
        //Log.v("IsItMyHash","HASH OF HOST "+hashOfHomePort);
        //Log.v("IsItMyHash",hostNode.hostPort+"**"+portList[0]);
        //Log.v("IsItMyHash predport",hostNode.predecessorPort);
        String hashOfPredPort = genHash(hostNode.predecessorPort);

        if(Integer.parseInt(hostNode.hostPort) == portList[0]){
            //Corner case
            if(hashOfKeyFile.compareTo(hashOfHomePort)<=0){
                //Log.i("IsItMyHash","MINE");
                return MINE;
            }else{
                //Log.i("IsItMyHash","NOT_MINE");
                return NOT_MINE;
            }
        }else{
            //Normal mode
            if(hashOfKeyFile.compareTo(hashOfPredPort) > 0
                    &&
                    hashOfKeyFile.compareTo(hashOfHomePort)<=0){
                Log.i("IsItMyHash","MINE "+hashOfKeyFile);
                Log.i("IsItMyHash","MINE "+hashOfPredPort);
                Log.i("IsItMyHash","MINE "+hashOfHomePort);
                return  MINE;
            }else{
                //Log.i("IsItMyHash","NOT_MINE");
                return NOT_MINE;
            }
        }
    }

    /*private boolean isItCornerCase(String hashOfHomePort) throws NoSuchAlgorithmException {

        String hashOfFirst = genHash(String.valueOf(portList[0]));

        if(hashOfHomePort.compareTo(hashOfFirst)==0){
            return true;
        }else {
            return false;
        }
    }*/

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
        TelephonyManager telpmngr = (TelephonyManager)getContext().getSystemService(getContext().TELEPHONY_SERVICE);
        String portStr = telpmngr.getLine1Number().substring(telpmngr.getLine1Number().length() - 4) ;
        final String myServerPort =  String.valueOf(Integer.parseInt(portStr) * 2);

        //Log.i("OnCreate()","--begin");

        homePort = String.valueOf(Integer.parseInt(myServerPort) / 2);
        hostNode.hostPort = homePort;
        hostNode.successorPort = mySuccessorPort(homePort);
        hostNode.predecessorPort = myPredecessorPort(homePort);
        //Log.i("OnCreate()","Port+"+homePort+"succ+"+hostNode.successorPort+"pred+"+hostNode.predecessorPort);
        for(int i = 0; i < portList.length; i++){
            if(Integer.parseInt(homePort)==portList[i]){
                portListCheck[i] = 999;
                break;
            }
        }

        //establish server irrespective of who or what the node is
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
            //Log.v("DHT: onCreate", "Creating a server (" + myServerPort + ")");
            //setting up node for self
            //myNode = new NodeInfo(null,null,genHash(getRightID(myServerPort)),myServerPort,null,null);

        } catch (IOException e) {
            Log.e("DHT: onCreate", "Can't create a ServerSocket");

        }
        return false;
    }
    /*
     * Returns predecessor of the Node
     * @author: Srinivasan Rajappa
     */
    private String myPredecessorPort(String portString) {
        int port = Integer.parseInt(portString);

        for(int i = 0; i < portList.length; i++){
            if(portList[i]==port){
                if(i==0){
                    return String.valueOf(portList[4]);
                }
                return String.valueOf(portList[(i-1)%5]);
            }
        }
        return null;
    }
    /*
     * Returns successor of the Node
     * @author: Srinivasan Rajappa
     */
    private String mySuccessorPort(String portString) {
        int port = Integer.parseInt(portString);

        for(int i = 0; i < portList.length; i++){
            if(portList[i]==port){
                if(i==4){
                    return String.valueOf(portList[0]);
                }
                return String.valueOf(portList[(i+1)%5]);
            }
        }
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO Auto-generated method stub
        String[] colVal = new String[]{"key","value"};
        MatrixCursor tbl = new MatrixCursor(colVal);

        int decision;
        Log.i("QUERY","starting here " + selection);
        try{
            if(selection.equals("*")){
                tbl = (MatrixCursor) getKeyValFromAll(tbl);
            }
            else if(selection.equals("@")) {
                tbl = (MatrixCursor) allKeyValInCurrentNode(tbl);
            }
            else{

                tbl = (MatrixCursor) findSingleKeyValuePair(tbl,selection);
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            return tbl;
        }
    }


    private Cursor findSingleKeyValuePair(MatrixCursor tbl,String selection) throws NoSuchAlgorithmException, IOException, InterruptedException {

        String hashOfSelection = genHash(selection);
        int decision = isItMyHash(selection);

        switch (decision){
            case MINE:      tbl.addRow(returnKeyValuePair(selection));
                keyValForSingle = new String[2];
                tempKey = null;
                tempKey = null;
                return tbl;
            case NOT_MINE:  tbl.addRow(returnKeyValuePairFromOther(selection));
                keyValForSingle = new String[2];
                tempKey = null;
                tempKey = null;
                return tbl;

            default: Log.v("Find a single key","Couldn't resovle");
        }

        return tbl;
    }
    /*
     * Find the id of the sender and seek key value pair
     * @author : Srinivasan Rajappa
     */
    private String[] returnKeyValuePairFromOther(String selection) throws NoSuchAlgorithmException, InterruptedException {

        int portOfTheOneWhoHash = estimateTheRecipient(genHash(selection));
        String requestForKeyValue = SEEK+"<>"+hostNode.hostPort+"<>"+selection;
        int counter = 0;

        Log.i("Query Single OTHERs",requestForKeyValue+" @@@ PORT "+String.valueOf(portList[portOfTheOneWhoHash]));

        List<Integer> l = new ArrayList<>();
        l.add(portList[portOfTheOneWhoHash]);
        l.add(portList[(portOfTheOneWhoHash+1)%5]);
        l.add(portList[(portOfTheOneWhoHash+2)%5]);
        for(int i = 0; i<l.size();i++){
            Log.v("Setting Arrival",l.get(i)+"");
        }
        forQueryArrivals.put(selection,l);
        forQueryKey.put(selection, "");


        /*arrivalStatus[0] = portList[portOfTheOneWhoHash];
        arrivalStatus[1] = portList[(portOfTheOneWhoHash+1)%5];
        arrivalStatus[2] = portList[(portOfTheOneWhoHash+2)%5];*/

        sendToTheNode(requestForKeyValue, portList[portOfTheOneWhoHash]);
        sendToTheNode(requestForKeyValue, portList[(portOfTheOneWhoHash+1) % 5]);
        sendToTheNode(requestForKeyValue, portList[(portOfTheOneWhoHash+2) % 5]);
        String keyValForSingleString = forQueryKey.get(selection);
        while(counter<=250 || !keyValForSingleString.isEmpty()){
            if(forQueryArrivals.get(selection).isEmpty()){
                conditionChanges = false;
                break;
            }else{
                Thread.sleep(20);
                counter++;
            }
            keyValForSingleString = forQueryKey.get(selection);
        }
        Log.i("QUERY S", "out of while loop");
        keyValForSingle[0] = selection;
        keyValForSingle[1] = forQueryKey.get(selection);
        Log.v("Query S received",keyValForSingle[0]+" "+keyValForSingle[1]);
        return keyValForSingle;
    }

    /*
     * Returns the Key Value pair in
     * String[]
     * @author: Srinivasan Rajappa
     *
     */
    private String[] returnKeyValuePair(String selection) throws IOException, InterruptedException {
        int index = -1;
        for(int i = 0 ; i < portList.length; i++){
            if(portList[i]==Integer.parseInt(homePort)){
                index = i;
                break;
            }
        }
        List<Integer> l = new ArrayList<>();
        l.add(portList[index]);
        l.add(portList[(index+1)%5]);
        l.add(portList[(index+2)%5]);
        for(int i = 0; i<l.size();i++){
            Log.v("Setting Arrival",l.get(i)+"");
        }
        //$$$$$$$4$$$$$$
        forQueryArrivals.put(selection,l);
        forQueryKey.put(selection, "");

        int myPort =  portList[index];
        int successorPort1 =  portList[(index+1)%5];
        int successorPort2 =  portList[(index+2)%5];

        int counter = 0;

        String msgToThisNodeAndOthers = SEEK+"<>"+String.valueOf(myPort)+"<>"+selection;

        sendToTheNode(msgToThisNodeAndOthers,myPort);
        sendToTheNode(msgToThisNodeAndOthers,successorPort1);
        sendToTheNode(msgToThisNodeAndOthers,successorPort2);
        String keyValForSingleString = forQueryKey.get(selection);
        while(counter<=250 || !keyValForSingleString.isEmpty()){
            if(forQueryArrivals.get(selection).isEmpty()){
                conditionChanges = false;
                break;
            }else{

                Thread.sleep(20);
                counter++;
            }
            keyValForSingleString = forQueryKey.get(selection);
        }
        Log.v("QUERY Reply","Out of while Loop all done "+keyValForSingle[0]);
        //If the timeout happens then we take the latest version of the
        // key and val
        keyValForSingle[0] = selection;
        keyValForSingle[1] = forQueryKey.get(selection);
        Log.v("Query S other","rcvd  "+keyValForSingle[0]);
        return keyValForSingle;
        /*
        String[] listOfFiles = new String[100];
        listOfFiles=getContext().fileList();
        Log.i("Query S SAME",selection);
        FileInputStream fpInput = null;
        String[] valueSet = new String[2];
        for(String fName: listOfFiles){
            if(fName.equals(selection)){
                fpInput = getContext().openFileInput(fName);
                BufferedReader br = new BufferedReader(new InputStreamReader(fpInput));
                valueSet[1] = br.readLine();
                valueSet[0] = fName;
                return valueSet;
            }
        }
        return valueSet;*/
    }

    /*
     * This will seek all the key values in the current node.
     * @author: Srinivasan Rajappa
     */
    private Cursor allKeyValInCurrentNode(MatrixCursor tbl) throws IOException {
        String[] listOfFiles = new String[100];

        listOfFiles=getContext().fileList();
        //Log.i("Query @","ALL WELL");
        FileInputStream fpInput = null;
        String[] valueSet = new String[2];
        for(String fName: listOfFiles){
            Log.i("FILES @",fName);
            fpInput = getContext().openFileInput(fName);
            BufferedReader br = new BufferedReader(new InputStreamReader(fpInput));
            valueSet[1] = br.readLine();
            valueSet[0] = fName;
            tbl.addRow(valueSet);

        }
        //Log.i("ALLKEY","s-- end");

        return tbl;
    }
    /*
     * This will seek all the key values across the system environment.
     * @author: Srinivasan Rajappa
     */
    private Cursor getKeyValFromAll(MatrixCursor tbl) throws InterruptedException, IOException {
        //Find the other nodes that are not needed
        String[] twoStrings = new String[2];
        for(int i = 0; i < portList.length; i++){
            if(Integer.parseInt(hostNode.hostPort) == portList[i]){
                continue;
            }
            sendToTheNode("BULK@" + hostNode.hostPort, portList[i]);
        }
        while(true){
            if(conditionChanges){
                conditionChanges = false;
                j=0;
                k=0;
                break;
            }else{
                Thread.sleep(30);
            }
        }
        try{
            tbl = (MatrixCursor) allKeyValInCurrentNode(tbl);
            for(int i = 0; i < allKeySet.length;i++){
                twoStrings[0] = allKeySet[i];
                twoStrings[1] = allValueSet[i];

                tbl.addRow(twoStrings);
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            for(int i=0; i < allKeySet.length;i++){
                allKeySet[i]=null;
                allValueSet[i] = null;
            }
        }
        return tbl;
    }
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }



    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];

                /*
                 * TODO: Fill in your server code that receives messages and passes them
                 * to onProgressUpdate().
                 */

            //--Srinivasan BEGIN


            while(true) {
                try {
                    Socket s = serverSocket.accept();
                    PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
                    BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String content = br.readLine();
                    if(content.contains(REPLICA)){
                        insertReplicaObtained(content);
                    }else if(content.contains(BULK)){
                        sendBulkToTheOneWhoRequests(content);
                    }else if(content.contains(REPLY)){
                        receiveAllandDecide(content);
                    }else if(content.contains(SEEK)){
                        findAndSendToTheRequester(content);
                    }else if(content.contains(RESPONSE)){
                        extractAndUseTheResponse(content);
                    }else if(content.contains(DELETE)){
                        deleteTheEntry(content);
                    }
                    Log.v("RECV ", content);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /*
     * The content of the string is the
     * file that will be deleted on inspection
     * @author: Srinivasan Rajappa
     */
    private void deleteTheEntry(String content) {

        String[] deleteKey = content.split("<>");
        String fileName = deleteKey[1];
        String[] listOfFiles = new String[100];
        listOfFiles=getContext().fileList();

        Log.i("Delete S in Replica ",fileName);
        boolean fileDeletedOrNot = false;
        for(String fName: listOfFiles) {
            fileDeletedOrNot = getContext().deleteFile(fName);
            /*if (fName.equals(fileName)) {
                fileDeletedOrNot = getContext().deleteFile(fileName);
            }*/
        }
        if (!fileDeletedOrNot) {
            Log.e("Delete", "File Does not exist");
        }else{
            Log.i("Delete in Replica","Successful  @ "+fileName);
        }
    }

    private void extractAndUseTheResponse(String content) {
        Log.i("QUERY S", "Recieved response " + content);
        String extract[]  = content.split("<>");
        String keyObtained = extract[1];
        String valObtained = extract[2];
        String portOfSender = extract[3];
        String timeStamp = extract[4];
        int ctr=0;
        /*tempKey = keyObtained;
        tempVal = valObtained;*/

        Log.i("Response Handle",timeStamp+" with "+latestTime+" "+timeStamp.compareTo(latestTime));
        //if(timeStamp.compareTo(latestTime)>=0){
            //Log.i("")
            forQueryKey.put(keyObtained, valObtained);
            /*tempKey = keyObtained;
            tempVal = valObtained;*/
            latestTime = timeStamp;
        //}
        //Zero the one that sent this message
        List<Integer> l = null;
        if(forQueryArrivals.containsKey(keyObtained))
            l = forQueryArrivals.get(keyObtained);

        for(int i =0; i<l.size();i++){
            if(l.get(i)==Integer.parseInt(portOfSender)){
                l.remove(i);
                Log.i("QUERY S", "REmoving " + portOfSender);
            }
        }
        forQueryArrivals.put(keyObtained,l);

        if(l.size()==2){
            keyValForSingle[0] = keyObtained;
            keyValForSingle[1] = forQueryKey.get(keyObtained);
            Log.i("RESPONSE FOUND", keyObtained + "  "+forQueryKey.get(keyObtained));
            latestTime = "AAAAAAAAAA";
            conditionChanges = true;
        }
        //Log.v("RCVD Response for SEEK",content);
        /*int j=0,counter=0;
        *//*for(String s : extract) {
            Log.i("Extract String for ", s);
        }*//*
        String keyObtained = extract[1];
        String valObtained = extract[2];
        String portOfSender = extract[3];
        String timeStamp = extract[4];
        //Log.v("Arrival Status ",arrivalStatus[0]+"  "+arrivalStatus[1]+"  "+arrivalStatus[2]);
        //Calculate the total arrivals
        for(j=0; j < arrivalStatus.length;j++){
            if(arrivalStatus[j]==0){
                counter++;
            }
        }
        Log.v("Number of Cleared ",counter+"");
        if(counter>=2){
            Log.v("Rcv Response Latest",minTime+" &&& "+tempKey);
            keyValForSingle[0]= tempKey;
            keyValForSingle[1]= tempVal;
            minTime = "aaaaaaaaa";
            arrivalStatus[2]= 0;
            conditionChanges = true;


        }else {
            for (int i = 0; i < arrivalStatus.length; i++) {
                if (arrivalStatus[i] == Integer.parseInt(portOfSender)) {
                    arrivalStatus[i] = 0;
                }
            }
            if(minTime.compareTo(timeStamp)<0){
                minTime = timeStamp;
                tempKey = keyObtained;
                tempVal = valObtained;
            }
        }*/

    }

    /*
     * Method will receive the KEY
     * and find the one in the list and then send.
     * @author: Srinivasan Rajappa
     */
    private void    findAndSendToTheRequester(String content) throws IOException {
        //Log.v("SEEK Requesting",content);
        String[] extractedString = content.split("<>");
        String requester = extractedString[1];
        String key = extractedString[2];
        String val = null;
        String[] listOfFiles = new String[100];


        listOfFiles=getContext().fileList();

        FileInputStream fpInput = null;
        String[] valueSet = new String[2];
        for(String fName: listOfFiles){
            if(fName.equals(key)) {
                fpInput = getContext().openFileInput(fName);
                BufferedReader br = new BufferedReader(new InputStreamReader(fpInput));
                val = br.readLine();
            }
        }
        Date dateAndTime = new Date();
        Log.v("SEEK Processed",key+" % "+val);
        String response = RESPONSE+"<>"+key+"<>"+val+"<>"+hostNode.hostPort+"<>"+dateAndTime.toString();
        sendToTheNode(response,Integer.parseInt(requester));
    }

    /*
     * Method will receive the KEY VAL pair
     * and keep track of the arrivals of the lot.
     * @author: Srinivasan Rajappa
     */
    private void receiveAllandDecide(String content) {
        String[] trailOfStrings = content.split("<>");
        String senderPort = trailOfStrings[1];
        int i;

        for(i = 2; i<trailOfStrings.length;i++){
            if(i%2==0){
                allKeySet[j] = trailOfStrings[i];
                j++;
            }else{
                allValueSet[k] = trailOfStrings[i];
                k++;
            }
        }


        for(i = 0; i < portList.length; i++){
            if(Integer.parseInt(senderPort)==portList[i]){
                portListCheck[i]=1;
                break;
            }
        }
        for(i = 0 ; i < portList.length; i++){
            if(portListCheck[i]==0){
                break;
            }
        }
        if(i>=4){
            conditionChanges = true;

        }
    }

    /*
     * Creates a String mass of KEY VALUE pairs
     * and then sends to the one who requested the
     * bulk.
     * @author: Srinivasan Rajappa
     */
    private void sendBulkToTheOneWhoRequests(String content) throws IOException {
        String[] identityOfSender = content.split("@");
        String portOfSender = identityOfSender[1];          //found the one who requested.

        String reply = REPLY+"<>"+hostNode.hostPort;

        String[] listOfFiles = new String[100];

        listOfFiles=getContext().fileList();

        FileInputStream fpInput = null;

        for(String fName: listOfFiles) {
            fpInput = getContext().openFileInput(fName);
            BufferedReader br = new BufferedReader(new InputStreamReader(fpInput));
            reply = reply+"<>"+fName+"<>"+br.readLine();
        }

        sendToTheNode(reply,Integer.parseInt(portOfSender));
    }

    /*
     * Insert Key Val pair by finding contents
     * of the string obtained by the sender node.
     * @author: Srinivasan Rajappa
     */
    private void insertReplicaObtained(String content) throws IOException {
        String[] splitString = content.split("#");

        String key = splitString[1];
        String val = splitString[2];
        Log.i("Replica Insert",key+"####"+val);
        FileOutputStream fpOutput = getContext().openFileOutput(key, Context.MODE_PRIVATE);
        byte[] bitVal = val.getBytes();
        fpOutput.write(bitVal);
        fpOutput.close();
        fpOutput.flush();
    }


    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... packet) {
            try {
                String remotePort = packet[1];
                //Log.i("DYNAMO CLIENT: CLIENT",packet[1]);
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(remotePort));

                String msgToSend = packet[0];
                // Log.i("DYNAMO CLIENT",msgToSend);
                OutputStream os = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os);

                BufferedWriter bos = new BufferedWriter(osw);
                bos.write(msgToSend);
                bos.flush();

                socket.close();
                Log.v("Dynamo: CLIENT SENT",msgToSend+" Sent to "+Integer.parseInt(remotePort)/2);
            } catch (UnknownHostException e) {
                Log.e("Dynamo", "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e("Dynamo", "ClientTask socket IOException");
            }

            return null;
        }
    }

}
