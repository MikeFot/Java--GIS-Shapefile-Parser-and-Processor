package com.michaelfotiadis.shpparser.util.system;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.widgets.List;

import com.michaelfotiadis.shpparser.constants.AppConstants;
import com.michaelfotiadis.shpparser.userinterface.layouts.MainParserLayout;

/**
 * 
 * @author Michael Fotiadis & Alex Schillings
 *
 */
public class Log {

	private static final int STREAM_ERR = 0;
	private static final int STREAM_OUT = 1;
	
	private static final String LEVEL_SPACER = "   ";
	private static final String LEVEL_0 = "";
	private static final String LEVEL_1 = "" + LEVEL_SPACER;	
	private static final String LEVEL_2 = LEVEL_1 + LEVEL_SPACER;
	private static final String LEVEL_3 = LEVEL_2 + LEVEL_SPACER;
	private static final String LEVEL_4 = LEVEL_3 + LEVEL_SPACER;
	
	private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");			
	private static StringBuilder output  = new StringBuilder();
	
	private static final String ERROR_PREFIX = "ERR: ";
	private static final String OUT_PREFIX = "OUT: ";
	
	public static void Err(String text, int indent_level, boolean show_to_user){
		log(STREAM_ERR, doIndentations(ERROR_PREFIX,text, indent_level));
		if (show_to_user) {
		logUI(indent_level, text);
		}
	}

	public static void Out(String text, int indent_level, boolean show_to_user){
		log(STREAM_OUT, doIndentations(OUT_PREFIX, text, indent_level));
		if (show_to_user) {
		logUI(indent_level, text);
		}
		
	}

	public static void Exception(Exception e, int indent_level){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		sw.toString();
        log(STREAM_ERR, doIndentations(ERROR_PREFIX, sw.toString(), indent_level));
	}
	
	public static void Exception(String text, Exception e, int indent_level){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		sw.toString();
		log(STREAM_ERR, doIndentations(ERROR_PREFIX, text, indent_level));
        log(STREAM_ERR, doIndentations(ERROR_PREFIX, sw.toString(), indent_level));
	}
	
	public static void Exception(Throwable t, int indent_level){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		sw.toString();
		log(STREAM_ERR, doIndentations(OUT_PREFIX, sw.toString(), indent_level));
	}
	
	public static String getOutput() {
		return output.toString();
	}
	
	private static String getIndents(int indentLevel){
		switch(indentLevel){
		case 0:
			return LEVEL_0;
		case 1:
			return LEVEL_1;
		case 2:
			return LEVEL_2;
		case 3:
			return LEVEL_3;
		case 4:
			return LEVEL_4;
		default:
			StringBuffer sb = new StringBuffer();
			for(int i = 0; i < indentLevel; i ++){
				sb.append(LEVEL_SPACER);
			}
			return sb.toString();
		}
	}
	
	private static void log(int stream, String text){
		String timestamp = df.format(new Date(System.currentTimeMillis())) + " - ";
		
		switch(stream){
		case STREAM_ERR:
			text = ERROR_PREFIX + text;
			text = text.replace(ERROR_PREFIX, timestamp + ERROR_PREFIX);
			
			System.err.println(text);
			break;
		case STREAM_OUT:
			text = OUT_PREFIX + text;
			text = text.replace(OUT_PREFIX, timestamp + OUT_PREFIX);
			
			System.out.println(text);
			break;
		}		
		
		writeToFile(AppConstants.LOG_FILENAME, text, true);
		output.append(text + "\n");
		
	}

	private static String doIndentations(String prefix, String str, int indentLevel){
		String result = getIndents(indentLevel) + str;
		String separator =  getLineEnding(str);
		
		if (!(separator.equals(""))){
			result = result.replace(separator, separator + prefix + getIndents(indentLevel)); //getIndents(indentLevel));
		} else {
			
		}
		
		return result;
	}
	
	private static void writeToFile(String filename, String text, Boolean append){
        try{
            
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename, append), "UTF-8");
            BufferedWriter fbw = new BufferedWriter(writer);
            fbw.write(text);
            fbw.newLine();
            fbw.close();
        }catch (Exception e) {
        	System.err.println("ERROR: Error when writing to '"+ filename +"': " + e.getMessage());
        }
    }
	
	private static String getLineEnding(String str){
		if(str.contains("\r\n")){
			return "\r\n";
		}else if(str.contains("\n")){
			return "\n";
		}else if(str.contains("\r")){
			return "\r";
		}
		return "";
	}
	
	private static void logUI(int indent_level, String text) {

		List status = MainParserLayout.getSTATUS_LIST().ergoList;
		if ( status != null && MainParserLayout.getDisplay() != null) {
			MainParserLayout.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					String indent = "";
					switch (indent_level) {
					case 0 : indent = "";
					case 1 : indent = " ";
					case 2 : indent = "  ";
					case 3 : indent = "   ";
					default : indent = "";
					}
					status.add(indent + text);
					status.setTopIndex(status.getItemCount() - 1); // ensure list always scrolls down
					MainParserLayout.getSTATUS_LIST().ergoList.redraw();
				}
			});

		}
	}
}
