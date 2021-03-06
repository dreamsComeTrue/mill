// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.controller

import javafx.scene.Node
import mill.controller.FlowState.FlowState
import mill.controller.states._
import mill.ui.MainContent
import mill.ui.views.FileSelectView.FileSelectViewMode
import mill.ui.views._

class StateManager {
  private val applicationProjectState = new ApplicationProjectState
  private val applicationStructureState = new ApplicationStructureState
  private val newResourceState = new NewResourceState
  private val settingsState = new SettingsState
  private val openResourceState = new OpenResourceState

  private var lastFlowState: FlowState = _

  private var actualState: ApplicationState = applicationProjectState
  private var lastState: ApplicationState = actualState
  private var appViewState: ApplicationState = _

  def setActualState(newFlowState: FlowState): Unit = {
    val realLastState = lastState

    lastState = actualState

    newFlowState match {
      case FlowState.APPLICATION_PROJECT => switchToProjectState()
      case FlowState.APPLICATION_STRUCTURE => switchToStructureState()
      case FlowState.NEW_RESOURCE => if (switchToNewResourceState(realLastState)) return
      case FlowState.OPEN_RESOURCE => if (switchToOpenResourceState(realLastState)) return
      case FlowState.SETTINGS => if (switchToSettingsState(realLastState)) return
    }

    lastFlowState = newFlowState
  }

  private def switchToSettingsState(realLastState: ApplicationState): Boolean = {
    var previousContent: Node = null

    if (actualState == applicationProjectState) {
      previousContent = switchBackToProjectState(settingsState)
      settingsState.setEnabled(true)
    }
    else if (actualState == applicationStructureState) {
      previousContent = switchBackToStructureState(settingsState)
      settingsState.setEnabled(true)
    }
    else if (actualState == newResourceState) return switchStateFromTo(FlowState.NEW_RESOURCE, FlowState.SETTINGS)
    else if (actualState == openResourceState) return switchStateFromTo(FlowState.OPEN_RESOURCE, FlowState.SETTINGS)
    else if (actualState == settingsState) {
      if (realLastState == applicationProjectState) {
        settingsState.setOnSecondViewCompleted((_: Void) => ProjectView.instance().showView(null))
        settingsState.process(ProjectView.instance().getEditorPane)
        settingsState.setOnSecondViewCompleted(null)

        lastFlowState = FlowState.APPLICATION_PROJECT
      }
      else if (realLastState == applicationStructureState) {
        settingsState.setOnSecondViewCompleted((_: Void) => StructureView.instance().showView(null))
        settingsState.process(StructureView.instance().getRootNode)
        settingsState.setOnSecondViewCompleted(null)

        lastFlowState = FlowState.APPLICATION_STRUCTURE
      }

      actualState = realLastState

      return true
    }

    settingsState.process(previousContent)
    actualState = settingsState

    false
  }

  private def switchToOpenResourceState(realLastState: ApplicationState): Boolean = {
    var previousContent: Node = null

    OpenResourceView.instance().refreshFileSelectView()

    FileSelectView.instance().initialize("", FileSelectViewMode.OPEN_FILE)

    if (actualState == applicationProjectState) {
      previousContent = switchBackToProjectState(openResourceState)

      openResourceState.setEnabled(true)
    }
    else if (actualState == applicationStructureState) {
      previousContent = switchBackToStructureState(openResourceState)

      openResourceState.setEnabled(true)
    }
    else if (actualState == newResourceState) return switchStateFromTo(FlowState.NEW_RESOURCE, FlowState.OPEN_RESOURCE)
    else if (actualState == settingsState) return switchStateFromTo(FlowState.SETTINGS, FlowState.OPEN_RESOURCE)
    else if (actualState == openResourceState) {
      if (realLastState == applicationProjectState) {
        openResourceState.setOnSecondViewCompleted((_: Void) => {
          ProjectView.instance().showView(null)
          FileSelectView.instance().setOpacity(1.0)
          FileSelectView.instance().setScaleX(1.0)
          FileSelectView.instance().setScaleY(1.0)
        })

        openResourceState.process(ProjectView.instance().getEditorPane)
        openResourceState.setOnSecondViewCompleted(null)
        lastFlowState = FlowState.APPLICATION_PROJECT
      }
      else if (realLastState == applicationStructureState) {
        openResourceState.setOnSecondViewCompleted((_: Void) => {
          StructureView.instance().showView(null)
          FileSelectView.instance().setOpacity(1.0)
          FileSelectView.instance().setScaleX(1.0)
          FileSelectView.instance().setScaleY(1.0)
        })

        openResourceState.process(StructureView.instance().getRootNode)
        openResourceState.setOnSecondViewCompleted(null)
        lastFlowState = FlowState.APPLICATION_STRUCTURE
      }

      actualState = realLastState

      return true
    }

    openResourceState.setOnFirstTransitionFinished((_: Void) => FileSelectView.instance().afterCreated())
    openResourceState.process(previousContent)
    actualState = openResourceState

    false
  }

  private def switchToNewResourceState(realLastState: ApplicationState): Boolean = {
    var previousContent: Node = null

    if (actualState == applicationProjectState) {
      previousContent = switchBackToProjectState(newResourceState)
      newResourceState.setEnabled(true)
    }
    else if (actualState == applicationStructureState) {
      previousContent = switchBackToStructureState(newResourceState)
      newResourceState.setEnabled(true)
    }
    else if (actualState == openResourceState) return switchStateFromTo(FlowState.OPEN_RESOURCE, FlowState.NEW_RESOURCE)
    else if (actualState == settingsState) return switchStateFromTo(FlowState.SETTINGS, FlowState.NEW_RESOURCE)
    else if (actualState == newResourceState) {
      if (realLastState == applicationProjectState) {
        newResourceState.setOnSecondViewCompleted((_: Void) => ProjectView.instance().showView(null))
        newResourceState.process(ProjectView.instance().getEditorPane)
        newResourceState.setOnSecondViewCompleted(null)
        lastFlowState = FlowState.APPLICATION_PROJECT
      }
      else if (realLastState == applicationStructureState) {
        newResourceState.setOnSecondViewCompleted((_: Void) => StructureView.instance().showView(null))
        newResourceState.process(StructureView.instance().getRootNode)
        newResourceState.setOnSecondViewCompleted(null)
        lastFlowState = FlowState.APPLICATION_STRUCTURE
      }
      actualState = realLastState
      return true
    }

    newResourceState.setOnFirstTransitionFinished((_: Void) => NewResourceView.instance().afterCreated())
    newResourceState.process(previousContent)
    newResourceState.setOnFirstTransitionFinished(null)

    actualState = newResourceState

    false
  }

  private def switchToStructureState(): Unit = {
    var previousContent: Node = null

    if (actualState == applicationProjectState) previousContent = ProjectView.instance()
    else if (actualState == newResourceState) {
      ProjectView.instance().showView(null)
      previousContent = ProjectView.instance()
    }
    else if (actualState == openResourceState) {
      ProjectView.instance().showView(null)
      previousContent = ProjectView.instance()
    }
    else if (actualState == settingsState) {
      ProjectView.instance().showView(null)
      previousContent = ProjectView.instance()
    }

    val structureView = StructureView.instance()
    val rootNode = structureView.getRootNode

    structureView.showView(null)

    rootNode.setOpacity(1.0)
    rootNode.setScaleX(1.0)
    rootNode.setScaleY(1.0)

    structureView.setVisible(true)

    applicationStructureState.process(previousContent)
    actualState = applicationStructureState
  }

  private def switchToProjectState(): Unit = {
    var previousContent: Node = null

    if (actualState == applicationStructureState) previousContent = StructureView.instance()
    else if (actualState == newResourceState) previousContent = StructureView.instance()
    else if (actualState == openResourceState) previousContent = StructureView.instance()
    else if (actualState == settingsState) previousContent = StructureView.instance()

    val projectView = ProjectView.instance()
    val editorPane = projectView.getEditorPane

    editorPane.setOpacity(1.0)
    editorPane.setScaleX(1.0)
    editorPane.setScaleY(1.0)
    projectView.showView(null)
    applicationProjectState.process(previousContent)
    actualState = applicationProjectState
  }

  def getLastFlowState: FlowState = lastFlowState

  def switchToLastState(): Unit = {
    setActualState(lastFlowState)
  }

  def bindStates(content: MainContent): Unit = {
    applicationProjectState.setContent(ProjectView.instance())
    applicationStructureState.setContent(StructureView.instance())
    settingsState.setContent(SettingsView.instance())
    newResourceState.setContent(NewResourceView.instance())
    openResourceState.setContent(OpenResourceView.instance())
  }

  private def switchBackToStructureState(state: ApplicationState): Node = {
    StructureView.instance().showView(state.getContent)

    appViewState = applicationStructureState

    StructureView.instance().getRootNode
  }

  private def switchBackToProjectState(state: ApplicationState): Node = {
    ProjectView.instance().showView(state.getContent)

    appViewState = applicationProjectState

    ProjectView.instance().getEditorPane
  }

  private def switchStateFromTo(fromState: FlowState, toState: FlowState): Boolean = {
    if (appViewState != null) lastState = appViewState

    setActualState(fromState)
    setActualState(toState)

    true
  }
}

object StateManager {
  private var _instance: StateManager = _

  def instance(): StateManager = {
    if (_instance == null) _instance = new StateManager

    _instance
  }
}