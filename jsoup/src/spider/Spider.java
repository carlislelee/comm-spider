package spider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

/**
 * Created by carlislelee on 2017/8/24.
 */
public class Spider {

    public static List<String> getLinks(Document doc) throws IOException {
        List<String> list = new ArrayList<>();
        if (doc.select("a[href]") != null && !doc.select("a[href]").isEmpty()) {
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String subUrl = link.attr("abs:href");
                if (null != subUrl && !"".equals(subUrl)) {
                    int index = subUrl.indexOf("#");
                    if (index > -1) {
                        subUrl = subUrl.substring(0, index);
                    }
                    list.add(subUrl);
                }
            }
        }
        return list;
    }

    static boolean match(String url, String host) throws MalformedURLException {
        if (url == null || host == null || url.isEmpty() || host.isEmpty()) {
            return false;
        }
        String[] fs = new URL(url).getHost().split("\\.", 2);
        String h = fs.length == 2 ? fs[1] : fs[0];
        return host.contains(h);
    }

    static void extractInfo(Document doc) {
        String titleResult = doc.title();//标题
        System.out.println("title:" + titleResult);
        String desc = doc.select("meta[name='description']").attr("content");
        System.out.println("desc:" + desc);
        String contentResult = doc.select("p").text();//带HTML标签的内容
        System.out.println("content:" + contentResult.replaceAll("\n", " "));
    }

    public static void main(String[] args) throws IOException {
        //
        String fileName = args[0];
        Integer pageNum = Integer.parseInt(args[1]);

        //
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line = null;
        List<Entry<String, String>> weblist = new LinkedList<>();
        while ((line = br.readLine()) != null) {
            String custid = line.split("\t")[0];
            String weburl = line.split("\t")[1];
            Entry<String, String> en = new AbstractMap.SimpleEntry(custid, weburl);
            weblist.add(en);
        }
        for (Entry<String, String> pair : weblist) {
            //
            String seed = pair.getValue();
            Set<String> all_url = new HashSet<>();
//            System.out.println("seed:" + seed);
            System.out.println("custid:" + pair.getKey());
            Document document = null;
            try {
                document = Jsoup.connect(seed).timeout(3000).get();
            } catch (Exception e) {
                continue;
            }
            if (document == null) {
                return;
            }
            //获取所有未爬取子链接
            Set<Document> level_01_doc = new HashSet<>();
            Set<String> level_2_doc = new HashSet<>();
            level_01_doc.add(document);
            all_url.add(seed);
            // 一级
            List<String> list = getLinks(document);
            // 根据一级页面获取二级页面
            for (String url : list) {
                if (!match(url, seed) && !all_url.contains(url)) {
                    continue;
                }
                if (all_url.size() >= pageNum) {
                    break;
                }
//                System.out.println("URL-" + all_url.size() + ":" + url);
                Document doc = null;
                try {
                    doc = Jsoup.connect(url).timeout(3000).get();
                } catch (Exception e) {
                    continue;
                }
                if (doc != null) {
                    level_01_doc.add(doc);
                    all_url.add(url);
                    level_2_doc.addAll(getLinks(doc));
                }
            }

            for (Document doc : level_01_doc) {
                extractInfo(doc);
            }
            
            for (String url : level_2_doc) {
                if (!match(url, seed) && !all_url.contains(url)) {
                    continue;
                }
                if (all_url.size() >= pageNum) {
                    break;
                }
//                System.out.println("URL-" + all_url.size() + ":" + url);
                Document doc = null;
                try {
                    doc = Jsoup.connect(url).timeout(3000).get();
                } catch (Exception e) {
                    continue;
                }
                if (doc == null) {
                    continue;
                }
                extractInfo(doc);
                all_url.add(url);
            }
        }

    }

}
