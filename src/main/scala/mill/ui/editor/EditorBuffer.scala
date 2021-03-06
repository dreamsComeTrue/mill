package mill.ui.editor

import java.io.{File, PrintWriter}
import java.lang

import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.AnchorPane
import mill.controller.AppController
import mill.resources.Resource
import mill.resources.settings.ApplicationSettings
import mill.ui.{FooterArea, TextEditor}
import org.apache.commons.io.FilenameUtils
import scalafx.beans.property.ObjectProperty

import scala.io.Source

/**
  * Created by Dominik 'squall' Jasiński on 2018-08-17.
  */
class EditorBuffer(var window: EditorWindow, var title: String) extends AnchorPane {
  private var path = title
  private var resource = new ObjectProperty[Resource]()
  private val textEditor = new TextEditor(title, "", title) //	Temporarily, until 'openFile' gets called
  private var file: File = _

  init()

  private def init(): Unit = {
    val focusedListener: ChangeListener[java.lang.Boolean] = (_: ObservableValue[_ <: java.lang.Boolean], _: java.lang.Boolean, _: java.lang.Boolean) => {
      if (isFocused) {
        AppController.instance().setActiveEditorWindow(EditorBuffer.this.window)

        EditorBuffer.this.window.setActiveBuffer(EditorBuffer.this)

        FooterArea.instance().getZoomSliderValueProperty.set((textEditor.getFontSize - FooterArea.MIN_FONT_SIZE) * 10.0)

        val pos = textEditor.getCaretPositionProperty
        FooterArea.instance().setPosLabel(pos.getParagraphIndex + 1, pos.getColumnPosition + 1)
      }
    }

    textEditor.focusedProperty.addListener(focusedListener)
    this.focusedProperty.addListener(focusedListener)

    AnchorPane.setBottomAnchor(textEditor, 0.0)
    AnchorPane.setTopAnchor(textEditor, 0.0)
    AnchorPane.setLeftAnchor(textEditor, 0.0)
    AnchorPane.setRightAnchor(textEditor, 0.0)

    textEditor.getCaretPositionProperty.paragraphIndexProperty().addListener(new ChangeListener[Integer] {
      override def changed(observableValue: ObservableValue[_ <: Integer], t: Integer, t1: Integer): Unit = {
        val pos = textEditor.getCaretPositionProperty
        FooterArea.instance().setPosLabel(pos.getParagraphIndex + 1, pos.getColumnPosition + 1)
      }
    })

    textEditor.getCaretPositionProperty.columnPositionProperty().addListener(new ChangeListener[Integer] {
      override def changed(observableValue: ObservableValue[_ <: Integer], t: Integer, t1: Integer): Unit = {
        val pos = textEditor.getCaretPositionProperty
        FooterArea.instance().setPosLabel(pos.getParagraphIndex + 1, pos.getColumnPosition + 1)
      }
    })

    textEditor.setOnScroll((event: ScrollEvent) => {
      if (event.isControlDown) {
        event.consume()

        var size = AppController.instance().getCurrentFontSize.doubleValue

        if (event.getDeltaY > 0) size += 1 else size -= 1

        FooterArea.instance().getZoomSliderValueProperty.set((size - FooterArea.MIN_FONT_SIZE) * 10.0)
      }
    })

    FooterArea.instance().getZoomSliderValueProperty.set((textEditor.getFontSize - FooterArea.MIN_FONT_SIZE) * 10.0)

    ApplicationSettings.instance().syntaxHighlightingEnabledProperty.addListener(new ChangeListener[lang.Boolean] {
      override def changed(observable: ObservableValue[_ <: lang.Boolean], oldValue: lang.Boolean, newValue: lang.Boolean): Unit = {
        textEditor.setSyntaxHighlightingEnabled(newValue)
      }
    })

    ApplicationSettings.instance().lineNumbersVisibleProperty.addListener(new ChangeListener[lang.Boolean] {
      override def changed(observable: ObservableValue[_ <: lang.Boolean], oldValue: lang.Boolean, newValue: lang.Boolean): Unit = {
        textEditor.showLineNumbers(newValue)
      }
    })

    ApplicationSettings.instance().highlightCurrentLineProperty.addListener(new ChangeListener[lang.Boolean] {
      override def changed(observable: ObservableValue[_ <: lang.Boolean], oldValue: lang.Boolean, newValue: lang.Boolean): Unit = {
        textEditor.showLineHighlight(newValue)
      }
    })

    textEditor.setSyntaxHighlightingEnabled(ApplicationSettings.instance().getSyntaxHighlightingEnabled)
    textEditor.showLineNumbers(ApplicationSettings.instance().getLineNumbersVisible)
    textEditor.showLineHighlight(ApplicationSettings.instance().getHighlightCurrentLine)

    getChildren.add(textEditor)
  }

  override def requestFocus(): Unit = {
    textEditor.requestFocus()
  }

  def getTextEditor: TextEditor = textEditor

  def getTitle: String = title

  def getPath: String = path

  def setWindow(window: EditorWindow): Unit = {
    this.window = window
  }

  def getWindow: EditorWindow = window

  def openFile(pathPar: String, res: Resource): Unit = {
    this.path = FilenameUtils.normalize(pathPar)
    if (path != null) {
      file = new File(path)
      this.resource.set(res)

      textEditor.setText(Source.fromFile(path).getLines.mkString)

      //  Associate SourceFx content with selected resource from project
      if (resource != null) {
        this.resource.get.setContent(textEditor.getText)
      }
    }
  }

  /**
    * Saves content of SourceFX control to associated file
    */
  def saveFile(): Unit = {
    if (file != null) {
      val text = textEditor.getText

      System.out.println(text + "<<<")

      new PrintWriter(file.getPath) {
        write(text)
        close()
      }
    }
  }

  def getResource: Resource = resource.get

  def resourceProperty: ObjectProperty[Resource] = resource
}
