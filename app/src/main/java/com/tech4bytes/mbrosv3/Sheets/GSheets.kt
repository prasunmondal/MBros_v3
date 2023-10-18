//package com.tech4bytes.mbrosv3.Sheets
//
//import android.os.AsyncTask
//import org.xml.sax.InputSource
//
//
//internal class RetrieveFeedTask : AsyncTask<String?, Void?, RSSFeed?>() {
//    private var exception: Exception? = null
//    protected override fun doInBackground(vararg urls: String): RSSFeed? {
//        return try {
//            val url = URL(urls[0])
//            val factory: SAXParserFactory = SAXParserFactory.newInstance()
//            val parser: SAXParser = factory.newSAXParser()
//            val xmlreader: XMLReader = parser.getXMLReader()
//            val theRSSHandler = RssHandler()
//            xmlreader.setContentHandler(theRSSHandler)
//            val ist = InputSource(url.openStream())
//            xmlreader.parse(ist)
//            theRSSHandler.getFeed()
//        } catch (e: Exception) {
//            exception = e
//            null
//        } finally {
//            ist.close()
//        }
//    }
//
//    override fun onPostExecute(feed: RSSFeed?) {
//        // TODO: check this.exception
//        // TODO: do something with the feed
//    }
//}