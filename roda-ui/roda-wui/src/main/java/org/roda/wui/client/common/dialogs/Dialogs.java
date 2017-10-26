/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.ip.RepresentationState;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.search.SearchSuggestBox;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

import config.i18n.client.ClientMessages;

public class Dialogs {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final String ADD_TYPE = "#__ADDNEW__#";

  private Dialogs() {
    // do nothing
  }

  public static void showConfirmDialog(String title, String message, String cancelButtonText, String confirmButtonText,
    final AsyncCallback<Boolean> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText(title);

    FlowPanel layout = new FlowPanel();
    Label messageLabel = new Label(message);
    Button cancelButton = new Button(cancelButtonText);
    Button confirmButton = new Button(confirmButtonText);
    FlowPanel footer = new FlowPanel();

    layout.add(messageLabel);
    layout.add(footer);
    footer.add(cancelButton);
    footer.add(confirmButton);

    dialogBox.setWidget(layout);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    cancelButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        callback.onSuccess(false);
      }
    });

    confirmButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        callback.onSuccess(true);
      }
    });

    dialogBox.addStyleName("wui-dialog-confirm");
    layout.addStyleName("wui-dialog-layout");
    footer.addStyleName("wui-dialog-layout-footer");
    messageLabel.addStyleName("wui-dialog-message");
    cancelButton.addStyleName("btn btn-link");
    confirmButton.addStyleName("btn btn-play");

    dialogBox.center();
    dialogBox.show();
  }

  public static void showInformationDialog(String title, final String message, String continueButtonText,
    boolean canCopyMessage) {
    showInformationDialog(title, message, continueButtonText, canCopyMessage, new NoAsyncCallback<Void>());
  }

  public static void showInformationDialog(String title, final String message, String continueButtonText,
    boolean canCopyMessage, final AsyncCallback<Void> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText(title);

    FlowPanel layout = new FlowPanel();
    HTML messageLabel;

    if (canCopyMessage) {
      messageLabel = new HTML("<pre><code id='command_message'>" + message + "</code></pre>");
      messageLabel.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          JavascriptUtils.copyToClipboard("command_message");
          Toast.showInfo(messages.copiedToClipboardTitle(), messages.copiedToClipboardMessage());
        }
      });

    } else {
      messageLabel = new HTML(message);
    }

    layout.add(messageLabel);

    Button continueButton = new Button(continueButtonText);
    layout.add(continueButton);

    dialogBox.setWidget(layout);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    continueButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        callback.onSuccess(null);
      }

    });

    dialogBox.addStyleName("wui-dialog-information");
    layout.addStyleName("wui-dialog-layout");
    messageLabel.addStyleName("wui-dialog-message");
    continueButton.addStyleName("btn btn-play");

    dialogBox.center();
    dialogBox.show();
  }

  public static void showPromptDialog(String title, String message, String value, String placeHolder,
    final RegExp validator, String cancelButtonText, String confirmButtonText, boolean mandatory,
    final AsyncCallback<String> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText(title);

    final FlowPanel layout = new FlowPanel();

    if (message != null) {
      final Label messageLabel = new Label(message);
      layout.add(messageLabel);
      messageLabel.addStyleName("wui-dialog-message");
    }

    final TextBox inputBox = new TextBox();
    inputBox.setTitle("input box");

    if (value != null) {
      inputBox.setText(value);
    }

    if (placeHolder != null) {
      inputBox.getElement().setPropertyString("placeholder", placeHolder);
    }

    final Button cancelButton = new Button(cancelButtonText);
    final Button confirmButton = new Button(confirmButtonText);
    confirmButton.setEnabled(!mandatory);

    layout.add(inputBox);
    layout.add(cancelButton);
    layout.add(confirmButton);

    dialogBox.setWidget(layout);
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        callback.onFailure(null);
      }
    });

    confirmButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (validator.test(inputBox.getText())) {
          dialogBox.hide();
          callback.onSuccess(inputBox.getText());
        }
      }
    });

    inputBox.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        boolean isValid = validator.test(event.getValue());
        confirmButton.setEnabled(isValid);
        if (isValid) {
          inputBox.removeStyleName("error");
        } else {
          inputBox.addStyleName("error");
        }
      }
    });

    inputBox.addKeyPressHandler(new KeyPressHandler() {
      @Override
      public void onKeyPress(KeyPressEvent event) {
        TextBox box = (TextBox) event.getSource();
        confirmButton.setEnabled(validator.test(box.getText()));
      }
    });

    inputBox.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        TextBox box = (TextBox) event.getSource();
        confirmButton.setEnabled(validator.test(box.getText()));
      }
    });

    inputBox.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          if (validator.test(inputBox.getText())) {
            dialogBox.hide();
            callback.onSuccess(inputBox.getText());
          }
        } else {
          TextBox box = (TextBox) event.getSource();
          confirmButton.setEnabled(validator.test(box.getText()));
        }
      }
    });

    dialogBox.addStyleName("wui-dialog-prompt");
    layout.addStyleName("wui-dialog-layout");
    inputBox.addStyleName("form-textbox wui-dialog-message");
    cancelButton.addStyleName("btn btn-link");
    confirmButton.addStyleName("pull-right btn btn-play");

    dialogBox.center();
    dialogBox.show();
    inputBox.setFocus(true);
  }

  public static void showPromptDialogSuggest(String title, String message, String placeHolder, String cancelButtonText,
    String confirmButtonText, SearchSuggestBox<?> suggestBox, final AsyncCallback<String> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText(title);

    final FlowPanel layout = new FlowPanel();

    if (message != null) {
      final Label messageLabel = new Label(message);
      layout.add(messageLabel);
      messageLabel.addStyleName("wui-dialog-message");
    }

    final SearchSuggestBox<?> inputBox = suggestBox;

    if (placeHolder != null) {
      inputBox.getElement().setPropertyString("placeholder", placeHolder);
    }

    final Button cancelButton = new Button(cancelButtonText);
    final Button confirmButton = new Button(confirmButtonText);

    layout.add(inputBox);
    layout.add(cancelButton);
    layout.add(confirmButton);

    dialogBox.setWidget(layout);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    cancelButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        callback.onFailure(null);
      }
    });

    confirmButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        callback.onSuccess(inputBox.getValue());
      }
    });

    dialogBox.addStyleName("wui-dialog-prompt");
    layout.addStyleName("wui-dialog-layout");
    inputBox.addStyleName("form-textbox wui-dialog-message");
    cancelButton.addStyleName("btn btn-link");
    confirmButton.addStyleName("pull-right btn btn-play");

    dialogBox.center();
    dialogBox.show();
    inputBox.setFocus(true);
  }

  public static void showPromptDialogEntityTypes(String title, String cancelButtonText, String confirmButtonText,
    List<String> types, boolean isControlledVocabulary, final AsyncCallback<String> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText(title);

    final FlowPanel layout = new FlowPanel();
    final ListBox select = new ListBox();

    for (String type : types) {
      select.addItem(type);
    }

    final Button cancelButton = new Button(cancelButtonText);
    final Button confirmButton = new Button(confirmButtonText);

    layout.add(select);

    final TextBox newTypeBox = new TextBox();
    final Label newTypeLabel = new Label(messages.entityTypeNewLabel() + ": ");
    newTypeBox.setVisible(false);
    newTypeLabel.setVisible(false);

    if (!isControlledVocabulary) {
      select.addItem(messages.entityTypeAddNew(), ADD_TYPE);

      newTypeBox.getElement().setPropertyString("placeholder", messages.entityTypeNewLabel());
      layout.add(newTypeLabel);
      layout.add(newTypeBox);
    }

    select.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        String selectedValue = select.getSelectedValue();
        newTypeLabel.setVisible(selectedValue.equals(ADD_TYPE));
        newTypeBox.setVisible(selectedValue.equals(ADD_TYPE));
      }
    });

    layout.add(cancelButton);
    layout.add(confirmButton);

    dialogBox.setWidget(layout);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    cancelButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        callback.onFailure(null);
      }
    });

    confirmButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();

        if (StringUtils.isNotBlank(newTypeBox.getText())) {
          callback.onSuccess(newTypeBox.getText());
        } else {
          callback.onSuccess(select.getSelectedValue());
        }
      }
    });

    dialogBox.addStyleName("wui-dialog-prompt");
    layout.addStyleName("wui-dialog-layout");
    select.addStyleName("form-textbox wui-dialog-message");
    cancelButton.addStyleName("btn btn-link");
    confirmButton.addStyleName("pull-right btn btn-play");
    newTypeBox.addStyleName("form-textbox wui-dialog-message");

    dialogBox.center();
    dialogBox.show();
    select.setFocus(true);
  }

  private static CheckBox getRepresentationStateCheckBox(String state, boolean value) {
    final CheckBox stateBox = new CheckBox();
    stateBox.setText(messages.statusLabel(state));
    stateBox.setFormValue(state);
    stateBox.setValue(value);
    stateBox.addStyleName("form-checkbox");
    return stateBox;
  }

  public static void showPromptDialogRepresentationStates(String title, String cancelButtonText,
    String confirmButtonText, List<String> states, final AsyncCallback<List<String>> callback) {
    final DialogBox dialogBox = new DialogBox(true, true);
    dialogBox.setText(title);

    final FlowPanel layout = new FlowPanel();
    final List<CheckBox> checkBoxes = new ArrayList<>();

    final Label label = new Label(messages.otherStatusLabel());
    final TextBox otherBox = new TextBox();
    otherBox.getElement().setPropertyString("placeholder", messages.otherStatusPlaceholder());
    otherBox.addStyleName("form-textbox wui-dialog-message");

    for (String state : states) {
      CheckBox stateBox = getRepresentationStateCheckBox(state, true);
      layout.add(stateBox);
      checkBoxes.add(stateBox);
    }

    for (String state : RepresentationState.values()) {
      if (!states.contains(state)) {
        CheckBox stateBox = getRepresentationStateCheckBox(state, false);
        layout.add(stateBox);
        checkBoxes.add(stateBox);
      }
    }

    layout.add(label);
    layout.add(otherBox);

    final Button cancelButton = new Button(cancelButtonText);
    final Button confirmButton = new Button(confirmButtonText);

    layout.add(cancelButton);
    layout.add(confirmButton);
    dialogBox.setWidget(layout);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        callback.onFailure(null);
      }
    });

    confirmButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        List<String> newStates = new ArrayList<>();

        for (CheckBox checkBox : checkBoxes) {
          if (checkBox.getValue()) {
            newStates.add(checkBox.getFormValue());
          }
        }

        if (StringUtils.isNotBlank(otherBox.getValue())) {
          newStates.add(otherBox.getValue());
        }

        callback.onSuccess(newStates);
      }
    });

    dialogBox.addStyleName("wui-dialog-prompt");
    layout.addStyleName("wui-dialog-layout");
    cancelButton.addStyleName("btn btn-link");
    confirmButton.addStyleName("pull-right btn btn-play");

    dialogBox.center();
    dialogBox.show();
  }

  public static DialogBox showLoadingModel() {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText("Loading...");

    FlowPanel layout = new FlowPanel();
    Label messageLabel = new Label(messages.executingTaskMessage());

    layout.add(messageLabel);

    dialogBox.setWidget(layout);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    dialogBox.addStyleName("wui-dialog-information");
    layout.addStyleName("wui-dialog-layout");
    messageLabel.addStyleName("wui-dialog-message");

    dialogBox.center();
    dialogBox.show();
    return dialogBox;
  }

}
