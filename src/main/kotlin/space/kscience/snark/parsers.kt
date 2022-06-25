package space.kscience.snark

import io.ktor.util.asStream
import io.ktor.utils.io.core.Input
import kotlinx.html.div
import kotlinx.html.unsafe
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import space.kscience.dataforge.io.IOReader
import space.kscience.dataforge.meta.Meta
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.reflect.KType
import kotlin.reflect.typeOf

abstract class SnarkTextParser<R> : SnarkParser<R> {
    abstract fun parseText(text: String, meta: Meta): R

    override fun parse(snark: SnarkPlugin, meta: Meta, bytes: ByteArray): R =
        parseText(bytes.decodeToString(), meta)
}


internal object SnarkHtmlParser : SnarkTextParser<HtmlFragment>() {
    override val fileExtensions: Set<String> = setOf("html")
    override val type: KType = typeOf<HtmlFragment>()

    override fun parseText(text: String, meta: Meta): HtmlFragment = {
        div {
            unsafe { +text }
        }
    }
}

internal object SnarkMarkdownParser : SnarkTextParser<HtmlFragment>() {
    override val fileExtensions: Set<String> = setOf("markdown", "mdown", "mkdn", "mkd", "md")
    override val type: KType = typeOf<HtmlFragment>()

    private val markdownFlavor = CommonMarkFlavourDescriptor()
    private val markdownParser = MarkdownParser(markdownFlavor)

    override fun parseText(text: String, meta: Meta): HtmlFragment {
        val parsedTree = markdownParser.buildMarkdownTreeFromString(text)
        val htmlString = HtmlGenerator(text, parsedTree, markdownFlavor).generateHtml()

        return {
            div {
                unsafe {
                    +htmlString
                }
            }
        }
    }
}

internal object ImageIOReader : IOReader<BufferedImage> {
    override val type: KType get() = typeOf<BufferedImage>()

    override fun readObject(input: Input): BufferedImage = ImageIO.read(input.asStream())
}
