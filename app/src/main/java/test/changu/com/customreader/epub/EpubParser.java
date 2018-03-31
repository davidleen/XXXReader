package test.changu.com.customreader.epub;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.text.TextUtils;
import android.util.Xml;

import com.changdu.util.Utils;
import com.changdu.zip.ZipJNIInterface;
import com.changdu.changdulib.util.Log;
import com.changdu.changdulib.util.storage.StorageUtils;

import test.changu.com.customreader.Log;

/**
 * ����epub�鱾��ϸ��Ϣ
 * @author chenli
 */
public class EpubParser {
	static String[] opsMetaLabels = { "title", "creator", "subject", "publisher", "language", "description", "right",
			"contributor" };
	
	private static String TAG_NORMAL_END = "\"";	
	private static String TAG_TEXT_BEGIN = "<text>";
	private static String TAG_TEXT_END = "</te";	
	private static String TAG_PLAY_ODER_BEGIN = "Order=\"";
	private static String TAG_SRC_BEGIN = "src=\"";

	String nameSpaceStr = "";
	String nameSapceCheck = "navMap";

	
	private String path;
	private EpubBook book;
	private List<EpubChapter> epubChapterList;
	private List<String> epubChapterTitle;
	private List<String> epubChapterPath;
	private int chapterIndex=0;
	private int chapterCount=0;
	private String temporaryRelativePath;
	
	private static EpubParser epubParser;
	private static String lastPath;
	private String tempPath;
	
	private EpubParser(String path){
		this.path = path;
		openEpub(path);
	}
	
	/**
	 * 获取Epub文件解析器(带缓存)
	 * 
	 * @param path
	 * @return
	 */
	public static EpubParser getEpubParser(String path){
		Log.d("getEpubParser begin ...");
		if(!TextUtils.isEmpty(path) && !path.equals(lastPath) || epubParser==null){
			epubParser = null;
			epubParser = new EpubParser(path);
			lastPath = path;
		}
		Log.d("getEpubParser end ...");
		return epubParser;
	}
	
	public static void releaseEpubParser() {
		epubParser = null;
	}
	
	/**
	 * 打开epub 文件, 如果解析失败则删除解析时产生的临时文件
	 * @param epubFilePath
	 * @return 是否成功打开
	 */
	public boolean openEpub(String epubFilePath){
		book = new EpubBook(epubFilePath);
		
		if (parserEpubFile(epubFilePath, getEpubTempPath(epubFilePath)) == false) {
			return false;
		}
		return true;
	}
	
	
	
	/**
	 * 解析Epub 文件
	 * @param epubFilePath 书籍位置
	 * @param exportFile 解析出的epub 各数据文件的位置
	 * @return
	 */
	private boolean parserEpubFile(String epubFilePath, String exportFile) {
		if(exportFile == null){
			exportFile = "";
		}
		StringBuffer stringBuffer = new StringBuffer(exportFile);
		stringBuffer.append("META-INF/container.xml");
		
		if (!unZipFile(epubFilePath, "META-INF/container.xml", stringBuffer.toString())) {
			return false;
		}
	
		String opfPath = getOpfPath(stringBuffer.toString());
		if (opfPath == null) {
			return false;
		}
		stringBuffer.delete(0, stringBuffer.length());
		stringBuffer.append(exportFile);
		stringBuffer.append(opfPath);
		
		book.setOpfPath(stringBuffer.toString());
		
		File opfFile = new File(book.getOpfPath());
		
		stringBuffer.delete(0, stringBuffer.length());
		stringBuffer.append(exportFile);
		stringBuffer.append("META-INF/pandaReader.ncx");
		File ncxFile = new File(stringBuffer.toString());	
		
		// NCX 文件不存在则需要从 opf文件解析章节信息
		boolean hasNcxFile = false;
		boolean needExportResource = false;
		if (!opfFile.exists()) {
			if (!unZipFile(epubFilePath, opfPath, book.getOpfPath())) {
				return false;
			}
			needExportResource = true;
		}

		if (!ncxFile.exists()) {
			String nxcPath = getNcxFilePath(book.getOpfPath());
			if (nxcPath != null && nxcPath.length() != 0) {
				if (!unZipFile(epubFilePath, nxcPath, stringBuffer.toString())) {
					return false;	
				}
				hasNcxFile = true;
			} else {
				stringBuffer.delete(0, stringBuffer.length());
			}
		} else {
			hasNcxFile = true;
		}
		
		Log.d("parseEpubOPF begin ... ... ");
		if (!parseEpubOPF(opfFile.getPath(), hasNcxFile, needExportResource)) {
			return false;
		}
		Log.d("parseEpubOPF end ... ... ");
		
		book.setNcxPath(stringBuffer.toString());
		Log.d("parseChapterInfo begin ... ... ");
		if (hasNcxFile && !parseChapterInfo(book.getNcxPath())) {
			return false;
		}
		Log.d("parseChapterInfo end ... ... ");
		book.setChapters(epubChapterList);
		parseChapterCount();
		parseChaptersTitle();
		parseChapterPath();
		
		return true;
	}

	
	private boolean unZipFile(String zipPath, String relativePath, String exportPath) {
		File exportFile = new File(exportPath);
		if (exportFile.exists()) {
			return true;
		}
		
		exportFile.getParentFile().mkdirs();
		if (ZipJNIInterface.UnZip(zipPath, relativePath, exportPath, "GBK")) {
			return true;
		}
		return false;
	}
	
	public String getEpubTempPath(String epubPath) {
		if (tempPath == null || tempPath.length() == 0) {
			File epubFile;
			try{
				epubFile = new File(epubPath);
				if (!epubFile.exists()) {
					return "";
				}
			}catch(NullPointerException e){
				e.printStackTrace();
				return "";
			}
			
			StringBuffer tempbBuffer = new StringBuffer(StorageUtils.getAbsolutePathIgnoreExist("/temp/Epub/"));
			
			int index = tempbBuffer.length();
			tempbBuffer.append(path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.')));
			tempbBuffer.append(epubFile.length());
			tempbBuffer.append("/");
			temporaryRelativePath = "/Epub/" + tempbBuffer.substring(index, tempbBuffer.length());
			StorageUtils.buildStoragePath(tempbBuffer.toString(), 0);		
			tempPath = tempbBuffer.toString();
		}
		return tempPath;
	}
	
	public static String getEpubCachePath(String epubPath) {
		File epubFile = new File(epubPath);
		if (!epubFile.exists()) {
			return null;
		}
		
		StringBuffer tempbBuffer = new StringBuffer(StorageUtils.getAbsolutePathIgnoreExist("/temp/Epub/"));
		tempbBuffer.append(epubPath.substring(epubPath.lastIndexOf('/') + 1, epubPath.lastIndexOf('.')));
		tempbBuffer.append(epubFile.length());
		tempbBuffer.append("/");
		StorageUtils.buildStoragePath(tempbBuffer.toString(), 0);		
		return tempbBuffer.toString();
	}
	
	public int getChapterIndex(){
		return chapterIndex;
	}
	
	/**
	 * 设置章节(自动解析)
	 * 
	 * @param chapterIndex
	 */
	public void setChapterIndex(int chapterIndex){
		this.chapterIndex = chapterIndex;
		parser(chapterIndex);
	}
	
	/**
	 * 获取章节总数
	 * 
	 * @return
	 */
	public int getChapterCount(){
		return chapterCount;
	}
	
	/**
	 * 获取上一显示章节
	 * 
	 * @param chapterIndex
	 * @return
	 */
	public int getEpubPreChapter(int chapterIndex){
		int preIndex = chapterIndex -1;
		EpubChapter epubChapter=null;
		while(preIndex>=0 && preIndex<chapterCount){
			epubChapter=epubChapterList.get(preIndex);
			if(!epubChapter.hasChild())
				break;
			preIndex--;
		}
		
		return preIndex;
	}
	/**
	 * 获取下一显示章节
	 * 
	 * @param chapterIndex
	 * @return
	 */
	public int getEpubNextChapter(int chapterIndex){
		int nextIndex = chapterIndex + 1;
		EpubChapter epubChapter=null;
		while(nextIndex>=0 && nextIndex<chapterCount){
			epubChapter=epubChapterList.get(nextIndex);
			if(!epubChapter.hasChild())
				break;
			nextIndex++;
		}
		return nextIndex;
	}

	/**
	 * 获取指定章节
	 * 
	 * @param chapterIndex
	 * @return
	 */
	public EpubChapter getEpubChapter(int chapterIndex){
		EpubChapter epubChapter=null;
		if(chapterIndex>=0 && chapterIndex<chapterCount){
			epubChapter=epubChapterList.get(chapterIndex);
			parser(chapterIndex);
		}
		
		return epubChapter;
	}
	
	/**
	 * 自动解析下一章节
	 * 
	 * @return
	 */
	public EpubChapter next(){
		EpubChapter epubChapter=null;
		if(chapterIndex+1>=0 && chapterIndex+1<chapterCount){
			epubChapter=epubChapterList.get(++chapterIndex);
			parser(chapterIndex);
		}
		
		return epubChapter;
	}
	
	/**
	 * 自动解析上一章节
	 * 
	 * @return
	 */
	public EpubChapter previous(){
		EpubChapter epubChapter=null;
		if(chapterIndex-1>=0 && chapterIndex-1<chapterCount){
			epubChapter=epubChapterList.get(--chapterIndex);
			parser(chapterIndex);
		}
		
		return epubChapter;
	}
	
	public String getTemporaryRelativePath(){
		return temporaryRelativePath;
	}
	
	private void parser(int chapterIndex){
		if(chapterIndex>=0 && chapterIndex<chapterCount){
			String relPath = getEpubTempPath(path) + epubChapterPath.get(chapterIndex);
			try{
				unZipFile(path, epubChapterPath.get(chapterIndex), relPath);
			}catch(Exception e){
				Log.e(e);
			}
		}
	}
	

	
	/**
	 * 获取解析后的epub实体
	 * 
	 * @return
	 */
	public EpubBook getEpub(){
		return book;
	}
	
	/**
	 * 获取章节路径列表
	 * 
	 * @return
	 */
	public List<String> getEpubChapterPath(){
		return epubChapterPath;
	}

	/**
	 * 获取章节名称列表
	 * 
	 * @return
	 */
	public List<String> getEpubChapterTitle(){
		return epubChapterTitle;
	}
	
	private void parseChapterCount(){
		chapterCount=epubChapterList==null || epubChapterList.isEmpty() ? 0 : epubChapterList.size();
	}
	
	private void parseChaptersTitle(){
		epubChapterTitle = new ArrayList<String>(chapterCount);
		for (int i = 0; i < chapterCount; i++) {
			epubChapterTitle.add(epubChapterList.get(i).getText());
		}
	}
	private void parseChapterPath() {
		epubChapterPath = new ArrayList<String>(chapterCount);
		for (int i = 0; i < chapterCount; i++) {
			epubChapterPath.add(epubChapterList.get(i).getSrc());
		}
	}
	
	/**
	 * Get path of the OPF file in the EPUB
	 * 
	 * @param containerPath container file path
	 * @return the path of the OPF
	 * @author chenli
	 * @modify by csy on 20111101
	 */
	private String getOpfPath(String containerPath) {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(containerPath));
			StringBuffer stringBuffer = new StringBuffer();
			String line = bufferedReader.readLine();
			while (line != null) {
				stringBuffer.append(line);
				line = bufferedReader.readLine();
			}
			
			bufferedReader.close();
			return getElement(stringBuffer, 0, "<rootfile full-path=\"", "\"");
		} catch (Exception e) {
			try {
				bufferedReader.close();
			} catch (IOException e1) {
				Log.e(e1);
			}
			Log.e(e);
		}
		return null;
	}

	/**
	 * parse the OPF file in the EPUB,and put the information to the EpubBook
	 * object
	 * 
	 * @param book
	 * @param hasNcxFile
	 *            :
	 */
	private boolean parseEpubOPF(String opfFilePath, boolean hasNcxFile, boolean needExportResource) {
		FileInputStream fileInput;
		BufferedInputStream in;
		boolean inManifest = false;
		boolean inMetadata = false;
		boolean getMetaData = false;
		boolean exportResourceComplete = false;
		try {
			fileInput = new FileInputStream(new File(opfFilePath));
			in = new BufferedInputStream(fileInput, 80960);
			XmlPullParser parser = Xml.newPullParser();
			
			String opfPath = book.getOpfPath();
			ArrayList<EpubChapter> tempChapter = new ArrayList<EpubChapter>();
			String opfDir = opfPath.substring(0, opfPath.lastIndexOf('/') + 1);
			int order = 0;
			boolean hasFindCover = false;
				String tempValeString = null;
				parser.setInput(in, "UTF-8");
				int event = parser.getEventType();
				while(event != XmlPullParser.END_DOCUMENT){
					switch(event){
					case XmlPullParser.END_TAG:
						if (!inMetadata && inManifest && ("manifest").equals(parser.getName())){
							inManifest = false;
						} else if (inMetadata == true && ("metadata").equals(parser.getName())) {
							inMetadata = false;
						}
						break;
					case XmlPullParser.START_TAG:
						if (!inMetadata && !inManifest && ("manifest").equals(parser.getName())){
							inManifest = true;
						} else if (!inMetadata && ("metadata").equals(parser.getName())) {
							inMetadata = true;
						}
						
						if (inMetadata) {
							String eventName = parser.getName();
							if (eventName.equals("title")){
								book.setTitle(parser.nextText());
							}else if (eventName.equals("creator")){
								book.setCreator(parser.nextText());
							}else if (eventName.equals("subject")){
								book.setSubjects(parser.nextText());
							}else if (eventName.equals("publisher")){
								book.setPublisher(parser.nextText());
							}else if (eventName.equals("language")){
								book.setLanguage(parser.nextText());
							}else if (eventName.equals("description")){
								book.setDescription(parser.nextText());
							}else if (eventName.equals("right")){
								book.setRights(parser.nextText());
							}
							getMetaData = true;
						} else if (inManifest) {
							if (!needExportResource && hasNcxFile) {
								if (getMetaData) {
									fileInput.close();
									in.close();
									return true;
								}
								break;
							}
							tempValeString = parser.getName();
							tempValeString = parser.getAttributeValue(null, "media-type");
							if (tempValeString != null) {
								tempValeString.toLowerCase();
								if (needExportResource) {
									if (tempValeString.equals("text/css")) {
										String tempPath = parser.getAttributeValue(null, "href");
										unZipFile(path, getRelativePath(opfFilePath, tempPath), opfDir+tempPath);
										break;
									} else { //if (tempValeString.startsWith("image/")) {    部分epub 没有遵照规定 有封面但是opf文档没有这个item
										if (!hasFindCover) {
											String coverPath = getCoverByDefaultPath(path, getEpubTempPath(path), true);
											book.setCoverPath(coverPath);
											hasFindCover = true;
										}

										if (!exportResourceComplete) {
											ParseResourceRunnable runable=new ParseResourceRunnable();
											Thread thread=new Thread(runable);
											thread.start();
											exportResourceComplete = true;
										}
										break;
									} 
								}
								if (!hasNcxFile && tempValeString.equals("application/xhtml+xml")) {
									EpubChapter chapter = new EpubChapter();
									chapter.setPri(1);
									chapter.setPlayOrder(order++);
									chapter.setText(parser.getAttributeValue(null, "id"));
									chapter.setSrc(getRelativePath(opfFilePath, parser.getAttributeValue(null, "href")));
									tempChapter.add(chapter);
								}
							} 
						}
						break;
					case XmlPullParser.START_DOCUMENT:
						tempChapter.clear();
						break;					
					default:
						break;
					}
					event = parser.next();
				}
				fileInput.close();
				in.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(e);
			}
		return true;
	}
	
	/**
	 * 从opf 文件中提起 ncx 文件的相对位置
	 * @param opfFilePath
	 * @return
	 */
	private String getNcxFilePath(String opfFilePath) {
		BufferedReader readerIn;
		String ncxPath = null;
		try {
			readerIn = new BufferedReader(new FileReader(opfFilePath));
			StringBuffer sBuffer = new StringBuffer(1024*3);
			String strFind = "\"application/x-dtbncx+xml\"";
			char[] charArray = new char[1024];
			int count = -1;
			int index = -1;
			while ((count = readerIn.read(charArray)) != -1) {
				sBuffer.append(charArray, 0, count);
				replaceXMLchar(sBuffer);
				index = sBuffer.indexOf(strFind);
				if (index == -1) {
					if (sBuffer.length() > 2048) {
						sBuffer.delete(0, 1024);
					}
					continue;
				} else {
					break;
				}
			}
			
			if (index != -1) {
				index = sBuffer.indexOf("/>", index);
				if (index != -1) {
					sBuffer.delete(index, sBuffer.length());
					int indexBegin = sBuffer.lastIndexOf("href");
					if (indexBegin != -1) {
						int indexNcxBegin = sBuffer.indexOf("\"", indexBegin);
						int indexNcxEnd = sBuffer.indexOf("\"", indexNcxBegin+1);
						if (indexNcxBegin != -1 && indexNcxEnd != -1) {
							ncxPath = sBuffer.substring(indexNcxBegin+1, indexNcxEnd);
						}
					} 
				}
			}
			readerIn.close();
		} catch (Exception e) {
			Log.e(e);
		}
		
		if (ncxPath != null && ncxPath.length() != 0) {
			int endIndex = opfFilePath.lastIndexOf('/');
			ncxPath = opfFilePath.substring(getEpubTempPath(path).length(), endIndex + 1) + ncxPath;
		}
		return ncxPath;
	}

	private static ArrayList<String> getLabelData(String source, String label) {
		ArrayList<String> results = new ArrayList<String>();
		String data = new String(source);
		int index = data.indexOf("<" + label);
		while (index >= 0) {
			char s = data.charAt(index + ("<" + label).length());
			if (s != ' ' && s != '>') {
				data = data.substring(index + ("<" + label).length() + 1);
				index = data.indexOf("<" + label);
				continue;
			}
			data = data.substring(index + label.length() + 1);
			int i1 = data.indexOf("/>");
			int i2 = data.indexOf("</");
			if (i1 >= 0 && i1 <= i2) {
				results.add("<" + label + data.substring(0, i1 + 2));
			} else if (i2 >= 0 && (i2 < i1 || i1 < 0)) {
				results.add("<" + label + data.substring(0, i2 + ("</" + label + ">").length()));
			}
			data = data.substring(data.indexOf("</" + label + ">") + ("</" + label + ">").length());
			index = data.indexOf("<" + label);
		}
		return results;
	}

	private String getAttribute(String data, String name) {
		String divider = "\"";
		if (data.indexOf("\"") < 0) {
			divider = "\'";
		}
		String rs="";
		String[] sources = data.split(" ");
		for (int i = 0; i < sources.length; i++) {
			if (sources[i].startsWith(name)) {
				rs+=sources[i];
				while(i<sources.length-1&&(!rs.endsWith(divider))){
					i++;
					rs+=" "+sources[i];
				}
				return rs.substring(name.length() + 2, rs.lastIndexOf(divider));
				// if (temp.contains("#")) {
				// return temp.substring(0, temp.lastIndexOf('#'));
				// } else {
				// return temp;
				// }
			}
		}
		return null;
	}

	private String getXMLData(String line, String label) throws IOException {
		int i = 0;
		// while (!line.contains("</" + label + ">") && !line.contains("<" +
		// label + "/>")
		// && !line.contains("<" + label + " />")) {
		// line += br.readLine();
		// }
		if ((i = line.indexOf("</")) >= 0) {
			line = line.substring(line.indexOf(">") + 1, i);
			if (line.indexOf("<![CDATA[") >= 0) {
				return line.substring(line.lastIndexOf("[") + 1, line.indexOf("]"));
			} else {
				return line;
			}
		} else {
			return null;
		}
	}

	
	private boolean parseChapterInfo(String ncxFilePath) {
		BufferedReader readerIn;
		if (epubChapterList == null) {
			epubChapterList = new ArrayList<EpubChapter>(512);
		}
		try {
			epubChapterList.clear();
			readerIn = new BufferedReader(new FileReader(ncxFilePath));
			StringBuffer sBuffer = new StringBuffer(1024*8);
			String strFindBegin = "<navPoint";
			String strFindEnd = "</navPoint>";
			String strTotalEnd = "</navMap>";
			String nameSpace = getNcxFileNameSpace(ncxFilePath);
			if (nameSpace != null) {
				strFindBegin = getTagName(strFindBegin, nameSpace);
				strFindEnd = getTagName(strFindEnd, nameSpace);
				strTotalEnd = getTagName(strTotalEnd, nameSpace);
			}
			char[] charArray = new char[1024*4];
			int endFinderLenger = strFindEnd.length();
			int count = -1;
			int start = -1;
			int end = -1;
			int nextStart = -1;
			int nextEnd = -1;
			int totalEnd = -1;	// </navMap> 全部结束的标志
			int searcherStart = 0;	// 指向stringBuffer 查找的起始位置
			
			int playOrder = 0;
			int chapterLevel = 1;
			boolean findComplete = false;
			while ((count = readerIn.read(charArray)) != -1 && !findComplete) {
				sBuffer.append(charArray, 0, count);
			//	replaceXMLchar(sBuffer);
				for (; ;) {
					if (start == -1) {
						start = sBuffer.indexOf(strFindBegin, searcherStart);
						end  = sBuffer.indexOf(strFindEnd, searcherStart);
						// </navPoint><navPoint xxxx
						if (end != -1 && (start == -1 || end < start)) {
							chapterLevel -= 1;
							searcherStart = 0;
							sBuffer.delete(0, end + endFinderLenger);
							start = -1;
							continue;
						}
						if (start == -1) {	
							if (sBuffer.length() > 4096) {
								searcherStart = 0;
								sBuffer.delete(0, sBuffer.length() - 20);	// 保留一小段头部，因为关键字有可能在两段buffer之间
							}
							break;
						}
					}
					
					end = sBuffer.indexOf(strFindEnd, start);
					nextStart = sBuffer.indexOf(strFindBegin, start+1);
					if (end != -1) {
						nextEnd = sBuffer.indexOf(strFindEnd, end+1);
						totalEnd = sBuffer.indexOf(strTotalEnd, end+1);
						//  </navPoint></navMap>
						if (totalEnd != -1 && nextEnd == -1 && nextStart == -1) {
							findComplete = true;
						}
					}

					// 字符串不够需要继续读取
					if (findComplete == false && (end == -1 && nextStart == -1)) {
						if (sBuffer.length() > 4096 * 2) {		// 数据超长控制
							return false;
						}
						break;
					}
					
					EpubChapter chapter = new EpubChapter();
					chapter.setPri(chapterLevel);
					//<navPoint xxxx <navPoint xxxx   或者 <navPoint xxxx <navPoint xxxx </navPoint>
					if (end == -1 || (nextStart!= -1 && end > nextStart)) {
						chapter.setHasChild(true);
						chapterLevel += 1;
					} else {
						chapter.setHasChild(false);
					}

					String tempString = getElement(sBuffer, start, TAG_PLAY_ODER_BEGIN, TAG_NORMAL_END);
					try {
						playOrder = Integer.valueOf(tempString);
					} catch (Exception e) {
						playOrder = 0;
						Log.e(e);
					}
					chapter.setPlayOrder(playOrder);
					chapter.setText(getElement(sBuffer, start, getTagName(TAG_TEXT_BEGIN, nameSpace), getTagName(TAG_TEXT_END, nameSpace)));
					chapter.setSrc(getRelativePath(book.getOpfPath(), getElement(sBuffer, start, TAG_SRC_BEGIN, TAG_NORMAL_END)));
					epubChapterList.add(chapter);

					if (chapter.hasChild()) {
						searcherStart = nextStart-1;
					} else {
						searcherStart = end+endFinderLenger;
					}
					if (sBuffer.length() >= 4000) {
						if (searcherStart > 3999) {
							Log.d(searcherStart);
							sBuffer.delete(0, searcherStart);
							searcherStart = 0;
						} else if (sBuffer.length() >= 8192) {
							Log.d(searcherStart);
							sBuffer.delete(0, searcherStart);
							searcherStart = 0;
						}
					}
					start = -1;
					end = -1;	
					nextStart = -1;
					nextEnd = -1;
					totalEnd = -1;
				}
			}
			readerIn.close();
			charArray = null;
			sBuffer = null;
		} catch (Exception e) {
			Log.e(e);
			return false;
		}
		
		return true;
	}
	
	private String getTagName(String name, String nameSpace) {
		if (nameSpace == null || name == null || name.equals("") || name.indexOf('<') == -1) {
			return name;
		}
		
		int index1 = name.indexOf('<');
		int index2 = name.indexOf("</");
		if (index2 != -1) {
			index1 += 1;
		}
		return name.substring(0, index1+1) + nameSpace + name.substring(index1+1);
	}
	
	private String getNcxFileNameSpace(String ncxFilePath) {
		BufferedReader readerIn = null;
		String nameSpace = null;
		try {
			readerIn = new BufferedReader(new FileReader(ncxFilePath));
			StringBuffer sBuffer = new StringBuffer(1024*8);
			char[] charArray = new char[1024*4];
			int count = -1;
			
			while ((count = readerIn.read(charArray)) != -1) {
				sBuffer.append(charArray, 0, count);
				
				nameSpace = getNameSpace(sBuffer, "navMap");
				if (nameSpace != null) {
					readerIn.close();
					return nameSpace;
				}
				sBuffer.delete(0, sBuffer.length() - "navMap".length());
			}
		} catch (Exception e) {
			Log.e(e);
		} finally {
			try {
				if (readerIn != null) {
					readerIn.close();
				}
			} catch (Exception e2) {
				Log.e(e2);
			}
		}
		return nameSpace;

	}
	
	/**
	 * get xml name space
	 * @param content
	 * @param checkWord
	 * @return null can note find nameSpace else return nameSpace
	 */
	private String getNameSpace(StringBuffer content, String checkWord) {
		int checkIndex = content.indexOf(checkWord);
		if (checkIndex != -1 && content.charAt(checkIndex-1) == ':') {
			int index1 = content.lastIndexOf("<", checkIndex);
			int index2 = content.lastIndexOf("/", checkIndex);
			if (index1 < index2) {
				index1 = index2;
			}
			return content.substring(index1+1, checkIndex);
		}
		return null;
	}
	
	
	private final String getElement(StringBuffer buffer, int findBegin, String ElementBegin,  String ElementEnd) {
		int indexBeing = buffer.indexOf(ElementBegin, findBegin);
		int indexEnd = buffer.indexOf(ElementEnd, indexBeing+ElementBegin.length());
		
		if (indexBeing != -1 && indexEnd != -1 && indexEnd > indexBeing) {
			return buffer.substring(indexBeing+ElementBegin.length(), indexEnd);
		}
		
		return "";
	}
	
	private class ParseResourceRunnable implements Runnable{

		public ParseResourceRunnable(){
		}
		
		@SuppressWarnings("unchecked")
        @Override
		public void run(){
			try{
				ArrayList<String> arrayListFile = (ArrayList<String>)ZipJNIInterface.getZipEntries(path);
				int count = arrayListFile.size();
				String imageFileHeadString = getRelativePath(book.getOpfPath(), "images/");
				String chapterFilePath;
				for (int i = 0; i < count; i++) {
					chapterFilePath = arrayListFile.get(i);
					if (isNeedExportImg(chapterFilePath, imageFileHeadString)) {
						if (!unZipFile(path, chapterFilePath, getEpubTempPath(path)+chapterFilePath)) {
							continue;
						}
					}
				}
			}catch(Exception e){
				Log.e(e);
			}
		}
	};
	
	private final boolean isNeedExportImg(String filePath, String imageFileHeadString) {
		if (imageFileHeadString.indexOf(imageFileHeadString) == 0 && !imageFileHeadString.endsWith("/")) {
			return true;
		}

		String path = filePath.toLowerCase();
		if (path.endsWith("gif") 
			|| path.endsWith("bmp") 
			|| path.endsWith("jpeg")
			|| path.endsWith("jpg")
			|| path.endsWith("png")) {
			return true;
		}
		return false;
	}
	
	public boolean exportFile(String filePath) {
		int index = filePath.indexOf("Epub/");
		index = filePath.indexOf('/', index + "Epub/".length()) + 1;
		return unZipFile(path, filePath.substring(index), filePath);
	}
	
	public boolean isEpubImageFolderFile(String filePath) {
		String imageFileHeadString = getRelativePath(book.getOpfPath(), "images/");
		return (filePath.indexOf(imageFileHeadString) == 0 && !filePath.endsWith("/"));
	}

	
	/**
	 * 根据多数EPub文件默认封面路径快速获取Epub封面
	 * 
	 * @param bookPath
	 * @param exportDir 将封面解压到目标路径
	 * @param useAllShortPath 是否将整个相当路径和 exportDir 合并作为输出文件的路径
	 * @return 获取成功，返回封面路径，否则返回null
	 */
	public static String getCoverByDefaultPath(String bookPath, String exportDir, boolean useAllShortPath) {
		String outputPath = exportDir;
		File file = new File(outputPath);
		if (!file.exists()){
			file.getParentFile().mkdirs();
		}
		ArrayList<String> arrayListFile = (ArrayList<String>)ZipJNIInterface.getZipEntries(bookPath);
		if (arrayListFile != null && arrayListFile.size() != 0) {
			int count = arrayListFile.size();
			String shorPathString = null;
			for (int i = 0; i < count; i++) {
				shorPathString = arrayListFile.get(i);
				if (shorPathString.endsWith("cover.jpg")
						|| shorPathString.endsWith("cover.png")
						|| shorPathString.endsWith("cover.jpeg")
						|| shorPathString.endsWith("cover.bmp")
						|| shorPathString.endsWith("cover.gif")) {
					outputPath += Utils.md5(bookPath);
					int index = shorPathString.lastIndexOf('/');
					if (index == -1 || useAllShortPath) {
						outputPath += shorPathString;
					} else {
						outputPath += shorPathString.substring(index+1);
					}
					if ((new File(outputPath)).exists()) {
						return outputPath;
					} else if (ZipJNIInterface.UnZip(bookPath, arrayListFile.get(i), outputPath, "GBK")) {
						return outputPath;
					}
				}
			}
		}

		return null;
	}
	/**
	 * 通过和临时根目录的比较获取相对路径的文件夹路径
	 * 例如 临时根目录路径是 /A/B/C/
	 *      输入的比较路径是 /A/B/C/D/E.ocx
	 *      输入的短路径是 F.html
	 *      则输出 D/F.html
	 * @param sourcePath
	 * @return
	 */
	private final String getRelativePath(String sourcePath, String shortPath) {
		int endIndex = sourcePath.lastIndexOf('/');
		return sourcePath.substring(getEpubTempPath(path).length(), endIndex + 1) + shortPath;
	}
	
	private final String getAbsolutePath(String sourcePath, String shortPath) {
		int endIndex = sourcePath.lastIndexOf('/');
		return sourcePath.substring(0, endIndex + 1) + shortPath;
	}
	
	private final void replaceXMLchar(StringBuffer sBuffer) {
		int begin = 0;
		int end = 0;
		while (begin != -1) {
			begin = sBuffer.indexOf("&", begin + 1);
			end = sBuffer.indexOf(";", begin);
			if (begin != -1 && end != -1 && end - begin <= 6) {
				switch (sBuffer.charAt(begin+1)) {
				case 'l':
					if (sBuffer.charAt(begin+2) == 't' && sBuffer.charAt(begin+3) == ';') {
						sBuffer.replace(begin, end+1, "<");
					}
					break;
				case 'g':
					if (sBuffer.charAt(begin+2) == 't' && sBuffer.charAt(begin+3) == ';') {
						sBuffer.replace(begin, end+1, ">");
					}
					break;
				case 'a':
					if (sBuffer.charAt(begin+2) == 'm' && sBuffer.charAt(begin+3) == 'p' && sBuffer.charAt(begin+4) == ';') {
						sBuffer.replace(begin, end+1, "&");
					} else if (sBuffer.charAt(begin+2) == 'p' && sBuffer.charAt(begin+3) == 'o' && sBuffer.charAt(begin+4) == 's') {
						sBuffer.replace(begin, end+1, "'");
					}
					break;
				case 'q':
					if (sBuffer.charAt(begin+2) == 'u' && sBuffer.charAt(begin+3) == 'o' && sBuffer.charAt(begin+4) == 't') {
						sBuffer.replace(begin, end+1, "\"");
					} 
					break;
				default:
					break;
				}
			}
		}
	}

}
