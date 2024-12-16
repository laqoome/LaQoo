package com.laqoome.laqoo.data.remote.parse

import android.net.Uri
import com.laqoome.laqoo.data.remote.dto.laqooBean
import com.laqoome.laqoo.data.remote.dto.laqooDetailBean
import com.laqoome.laqoo.data.remote.dto.EpisodeBean
import com.laqoome.laqoo.data.remote.dto.HomeBean
import com.laqoome.laqoo.data.remote.dto.VideoBean
import com.laqoome.laqoo.data.remote.parse.util.WebViewUtil
import com.laqoome.laqoo.util.DownloadManager
import com.laqoome.laqoo.util.getDefaultDomain
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object GirigiriSource : laqooSource {

    private const val LOG_TAG = "GirigiriSource"

    override val DEFAULT_DOMAIN: String = "https://laqoo.girigirilove.com"
    override var baseUrl: String = getDefaultDomain()
    private val webViewUtil: WebViewUtil by lazy { WebViewUtil() }

    override fun onExit() {
        webViewUtil.clearWeb()
    }

    override suspend fun getSearchData(query: String, page: Int): List<laqooBean> {
        val source = DownloadManager.getHtml("${baseUrl}/search/${query}----------${page}---/")
        val document = Jsoup.parse(source)
        val laqooList = mutableListOf<laqooBean>()
        document.select("div.public-list-box").forEach { el ->
            val title = el.select("div.thumb-txt").text()
            val url = el.select("a").attr("href")
            val imgUrl = el.select("img").attr("data-src").padDomain()
            laqooList.add(laqooBean(title = title, img = imgUrl, url = url))
        }
        return laqooList
    }

    override suspend fun getWeekData(): MutableMap<Int, List<laqooBean>> {
        val source = DownloadManager.getHtml(baseUrl)
        val document = Jsoup.parse(source)
        val elements = document.select("div.wow")[0].select("div#week-module-box")
        val weekMap = mutableMapOf<Int, List<laqooBean>>()
        elements.select("div.public-r").forEachIndexed { index, element ->
            val dayList = getlaqooList(element.select("div.public-list-box"))
            weekMap[index] = dayList
        }
        return weekMap
    }

    override suspend fun getHomeData(): List<HomeBean> {
        val source = DownloadManager.getHtml(baseUrl)
        val document = Jsoup.parse(source)
        val elements = document.select("div.wow").apply { removeAt(0) }
        val homeBeanList = mutableListOf<HomeBean>()
        for ((i, el) in elements.withIndex()) {
            if (i == 1) continue
            val title = el.select("div.title-left > h4").text()
            val moreUrl = el.select("div.title-right > a").attr("href")
            val homeItemBeanList = getlaqooList(el.select("div.public-list-box"))
            homeBeanList.add(HomeBean(title = title, moreUrl = moreUrl, laqoos = homeItemBeanList))
        }

        return homeBeanList
    }

    override suspend fun getlaqooDetail(detailUrl: String): laqooDetailBean {
        val source = DownloadManager.getHtml("${baseUrl}/$detailUrl")
        val document = Jsoup.parse(source)
        val main = document.select("div.vod-detail")
        val title = main.select("h3").text()
        val desc = main.select("div#height_limit").text()
        val imgUrl = main.select("img").attr("data-src").padDomain()
        val tags =
            main.select("div.slide-info").last()?.select("a")?.map { it.text() }?.toMutableList()
                ?.also { it.removeAt(it.lastIndex) } ?: emptyList()
        val channels = getlaqooEpisodes(document.select("div.anthology-list").select("ul"))
        val relatedlaqoos =
            getlaqooList(document.select("div.box-width.wow").select("div.public-list-box"))
        return laqooDetailBean(title, imgUrl, desc, tags, relatedlaqoos, channels = channels)
    }

    private fun getlaqooList(elements: Elements): List<laqooBean> {
        val laqooList = mutableListOf<laqooBean>()
        elements.forEach { el ->
            el.select("div.public-list-div > a").apply {
                val title = attr("title")
                val url = attr("href")
                val imgUrl = select("img").attr("data-src").padDomain()
                val episodeName = select("span.public-list-prb").text()
                laqooList.add(
                    laqooBean(
                        title = title,
                        img = imgUrl,
                        url = url,
                        episodeName = episodeName
                    )
                )
            }
        }
        return laqooList
    }

    override suspend fun getVideoData(episodeUrl: String): VideoBean {
        val url = "${baseUrl}/$episodeUrl"
        val source = DownloadManager.getHtml(url)
        val document = Jsoup.parse(source)
        /*var elements = document.select("div.player-right")
        if (elements.isEmpty()) {
            elements = document.select("div.player-info")
        }
        val title = elements.select("h2").text()
        var episodeName = ""
        val episodes = getlaqooEpisodes(
            elements.select("div.anthology-list-box").select("ul"),
            action = { episodeName = it })*/
        val videoUrl = getVideoUrl(document)
        return VideoBean(videoUrl)
    }

    @OptIn(ExperimentalEncodingApi::class)
    private suspend fun getVideoUrl(document: Document): String {
        val videoUrlTarget = document.select("div.player-box > div.player-left > script")[0].data()
        val videoUrlRegex = """"url":"(.*?)","url_next"""".toRegex()
        val rawVideoUrl = videoUrlRegex.find(videoUrlTarget)?.groupValues?.get(1)
            ?: throw IllegalStateException("video url is empty")

        val encodedVideoUrl = String(Base64.decode(rawVideoUrl), Charsets.UTF_8)
        return Uri.decode(encodedVideoUrl)
    }

    private fun getlaqooEpisodes(elements: Elements): Map<Int, List<EpisodeBean>> {
        val channels = mutableMapOf<Int, List<EpisodeBean>>()
        elements.forEachIndexed { i, e ->
            val dramaElements = e.select("li").select("a")//剧集列表
            val episodes = mutableListOf<EpisodeBean>()
            dramaElements.forEach { el ->
                val name = el.text()
                val url = el.attr("href")
                episodes.add(EpisodeBean(name, url))
            }
            channels[i] = episodes
        }

        return channels
    }

    private fun String.padDomain(): String {
        return "$baseUrl$this"
    }
}