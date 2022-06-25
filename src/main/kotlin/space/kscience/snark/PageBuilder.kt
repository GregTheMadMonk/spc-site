package space.kscience.snark

import space.kscience.dataforge.context.ContextAware
import space.kscience.dataforge.data.*
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.string
import space.kscience.dataforge.names.*

internal fun Name.toWebPath() = tokens.joinToString(separator = "/"){
    if (it.hasIndex()) {
        "${it.body}[${it.index}]"
    } else {
        it.body
    }
}

interface PageBuilder : ContextAware {
    val data: DataTree<*>

    val meta: Meta

    fun resolveRef(ref: String): String

    fun resolvePageRef(pageName: Name): String
}


fun PageBuilder.resolvePageRef(pageName: String) = resolvePageRef(pageName.parseAsName())

val PageBuilder.homeRef get() = resolvePageRef(SiteBuilder.INDEX_PAGE_TOKEN.asName())

/**
 * Resolve a Html builder by its full name
 */
fun DataTree<*>.resolveHtml(name: Name): HtmlData? {
    val resolved = (getByType<HtmlFragment>(name) ?: getByType<HtmlFragment>(name + SiteBuilder.INDEX_PAGE_TOKEN))

    return resolved?.takeIf {
        it.published //TODO add language confirmation
    }
}

/**
 * Find all Html blocks using given name/meta filter
 */
fun DataTree<*>.resolveAllHtml(predicate: (name: Name, meta: Meta) -> Boolean): Map<Name, HtmlData> =
    filterByType<HtmlFragment> { name, meta ->
        predicate(name, meta)
                && meta["published"].string != "false"
        //TODO add language confirmation
    }.asSequence().associate { it.name to it.data }


fun DataTree<*>.findByContentType(contentType: String, baseName: Name = Name.EMPTY) = resolveAllHtml { name, meta ->
    name.startsWith(baseName) && meta["content_type"].string == contentType
}

internal val Data<*>.published: Boolean get() = meta["published"].string != "false"