package lang;

import java.io.*;
import java.util.HashMap;
//import java.math.BigDecimal;
import java.util.*;
//import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.base.Charsets;
import com.google.common.io.Files;


public class lang {

	public static JSONObject langObj;	
	public static String LANG = "rus";
	//public static String[] toTranslate;
	public static HashMap <String, String> toTranslate = new HashMap<String, String>(); 
	
	
	public static void main(String[] args) 
	{
		
	//	private JSONObject peersJSON1;	
	
	//33333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333	
/*	File myFile = new File("Lang.txt"); //Create a file object and hold the name of our file.
	Scanner readThis = new Scanner(myFile); //Scanner object to read from the file
	ArrayList<String> temp = new ArrayList<String>(); //arraylist for the purpose of resizing array
//	StringTokenizer stok; //Not needed i believe
//	PrintWriter output = new PrintWriter("Yalla.txt"); //create a printwriter object so we can write to the specified file "Yalla.txt"
//	int i = 0; //Used as incrementor

	while(readThis.hasNext()) //while the file has something to be read
	{
	String str = readThis.next(); //Here will read each peice of text and will seperate by spaces
	//stok = new StringTokenizer(str) --> possibly not needed
	temp.add(str); //add to the arraylist object //add that text into our arraylist
	}
//	while(!temp.isEmpty()) //while our arraylist is not empty execute
//	{
//	output.print(temp.get(i)); //print to the output object("Yalla.txt") the first element (0)
//	i++; //increase the element to get the next.
//	}
	*/
		/*
		//OPEN FILE
		File file = new File("Lang.txt");
		
		
		
		//CREATE FILE IF IT DOESNT EXIST
	
			//READ PEERS FILE
			List<String> lines = Files.readLines(file, Charsets.UTF_8);
			
			String jsonString = "";
			for(String line : lines){
				jsonString += line;
			}
			
			//CREATE JSON OBJECT
			peersJSON = (JSONObject) JSONValue.parse(jsonString);
		
	
		*/	
			
		Set("rus");
			
		try {
			langObj = OpenLangFile("rus.lng");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	return;	
	};

	public static void Set(String lang)
	{
		LANG = lang;
	}
	public static String Translate(String Message) 
	{
		toTranslate.put(Message, ""); // запомним все строки что требуют перевода
		if (langObj == null ) return (Message); // если файл не открыт то не переводим 
		if (!langObj.containsValue(LANG)) return (Message); // если в открытом файле нет ключа установленого языка то не переводим 
		if (!langObj.containsKey(Message))  return (Message); // если нет такого словосочетания не переводим
		
		String res = langObj.get(Message).toString();
		toTranslate.put(Message, res); // запомним все строки что требуют перевода
		return(langObj.get(Message).toString());	//  если есть такое словосочетание то переводим.		
	}
	public static JSONObject OpenLangFile(String PathAndFilename) throws IOException
	{
		File file = new File(PathAndFilename);
		if (!file.isFile())	return((JSONObject) JSONValue.parse(""));
		
		List<String> lines = Files.readLines(file, Charsets.UTF_8);
		
		String jsonString = "";
		for(String line : lines){
			jsonString += line;
		}
		
		//CREATE JSON OBJECT
		return ((JSONObject) JSONValue.parse(jsonString));
	};

}
