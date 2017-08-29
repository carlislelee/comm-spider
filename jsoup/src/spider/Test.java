/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author lizhaoxi
 */
public class Test {

    public static void main(String[] args) {
        Document document = null;
        try {
            document = Jsoup.connect("http://fenxi.360.cn").timeout(3000).get();
        } catch (Exception e) {
        }
        if (document == null) {
            return;
        }
        String descString=document.select("meta[name='description']").get(0).attr("content");
                //getElementsByTag("content").toString();
        System.out.println(descString);
    }
}
