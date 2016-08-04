/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.planning;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.risks.Risk.SEVERITY_LEVEL;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.risks.RiskIncidence.INCIDENCE_STATUS;
import org.roda.wui.client.browse.Browse;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.ViewRepresentation;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;

import config.i18n.client.ClientMessages;

public class EditRiskIncidence extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        String riskIncidenceId = historyTokens.get(0);
        BrowserService.Util.getInstance().retrieve(RiskIncidence.class.getName(), riskIncidenceId,
          new AsyncCallback<RiskIncidence>() {

            @Override
            public void onFailure(Throwable caught) {
              callback.onFailure(caught);
            }

            @Override
            public void onSuccess(RiskIncidence incidence) {
              EditRiskIncidence editIncidence = new EditRiskIncidence(incidence);
              callback.onSuccess(editIncidence);
            }
          });
      } else {
        Tools.newHistory(FormatRegister.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(RiskIncidenceRegister.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "edit_riskincidence";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, EditRiskIncidence> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private RiskIncidence incidence;
  private static ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  Button buttonApply;

  @UiField
  Button buttonCancel;

  @UiField
  Label incidenceId;

  @UiField
  Label objectLabel;

  @UiField
  Anchor objectLink;

  @UiField
  Anchor riskLink;

  @UiField
  Label detectedOn, detectedBy;

  @UiField
  TextArea description;

  @UiField
  ListBox status, severity;

  @UiField
  DateBox mitigatedOn;

  @UiField
  TextBox mitigatedBy;

  @UiField
  TextArea mitigatedDescription;

  /**
   * Create a new panel to create a user
   *
   * @param user
   *          the user to create
   */
  public EditRiskIncidence(RiskIncidence incidence) {
    this.incidence = incidence;

    initWidget(uiBinder.createAndBindUi(this));

    incidenceId.setText(incidence.getId());

    if (AIP.class.getSimpleName().equals(incidence.getObjectClass())) {
      objectLabel.setText(messages.showAIPExtended());
      objectLink.setHref(Tools.createHistoryHashLink(Browse.RESOLVER, incidence.getAipId()));
      objectLink.setText(incidence.getAipId());

    } else if (Representation.class.getSimpleName().equals(incidence.getObjectClass())) {
      BrowserService.Util.getInstance().getRepresentationFromId(incidence.getRepresentationId(),
        new AsyncCallback<IndexedRepresentation>() {

          @Override
          public void onFailure(Throwable caught) {
            // do nothing
          }

          @Override
          public void onSuccess(IndexedRepresentation result) {
            if (result != null) {
              objectLabel.setText(messages.showRepresentationExtended());
              objectLink.setHref(Tools.createHistoryHashLink(Browse.RESOLVER,
                ViewRepresentation.RESOLVER.getHistoryToken(), result.getAipId(), result.getUUID()));
              objectLink.setText(result.getUUID());
            }
          }
        });

    } else if (File.class.getSimpleName().equals(incidence.getObjectClass())) {
      BrowserService.Util.getInstance().getFileFromId(incidence.getFileId(), new AsyncCallback<IndexedFile>() {

        @Override
        public void onFailure(Throwable caught) {
          // do nothing
        }

        @Override
        public void onSuccess(IndexedFile result) {
          if (result != null) {
            objectLabel.setText(messages.showFileExtended());
            objectLink
              .setHref(Tools.createHistoryHashLink(Browse.RESOLVER, ViewRepresentation.RESOLVER.getHistoryToken(),
                result.getAipId(), result.getRepresentationUUID(), result.getUUID()));
            objectLink.setText(result.getUUID());
          }
        }
      });
    }

    String riskId = incidence.getRiskId().replace("[", "").replace("]", "");
    riskLink.setText(riskId);
    riskLink.setHref(Tools.createHistoryHashLink(RiskRegister.RESOLVER, ShowRisk.RESOLVER.getHistoryToken(), riskId));

    detectedOn
      .setText(DateTimeFormat.getFormat(RodaConstants.DEFAULT_DATETIME_FORMAT).format(incidence.getDetectedOn()));
    detectedBy.setText(incidence.getDetectedBy());

    DefaultFormat dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));
    mitigatedOn.setFormat(dateFormat);
    mitigatedOn.getDatePicker().setYearArrowsVisible(true);
    mitigatedOn.setFireNullValues(true);
    mitigatedOn.setValue(incidence.getMitigatedOn());

    description.setText(incidence.getDescription());
    mitigatedBy.setText(incidence.getMitigatedBy());
    mitigatedDescription.setText(incidence.getMitigatedDescription());

    int selectedIndex = 0;
    for (INCIDENCE_STATUS istatus : INCIDENCE_STATUS.values()) {
      status.addItem(messages.riskIncidenceStatusValue(istatus), istatus.toString());

      if (istatus.equals(incidence.getStatus())) {
        selectedIndex = status.getItemCount();
      }
    }

    status.setSelectedIndex(selectedIndex - 1);

    selectedIndex = 0;
    for (SEVERITY_LEVEL iseverity : SEVERITY_LEVEL.values()) {
      severity.addItem(messages.severityLevel(iseverity), iseverity.toString());

      if (iseverity.equals(incidence.getSeverity())) {
        selectedIndex = severity.getItemCount();
      }
    }

    severity.setSelectedIndex(selectedIndex - 1);
  }

  public void getRiskIncidence() {
    incidence.setDescription(description.getText());
    incidence.setStatus(INCIDENCE_STATUS.valueOf(status.getSelectedValue()));
    incidence.setSeverity(SEVERITY_LEVEL.valueOf(severity.getSelectedValue()));
    incidence.setMitigatedOn(mitigatedOn.getValue());
    incidence.setMitigatedBy(mitigatedBy.getText());
    incidence.setMitigatedDescription(mitigatedDescription.getText());
  }

  public void clear() {
    description.setText("");
    mitigatedOn.setValue(null);
    mitigatedBy.setText("");
    mitigatedDescription.setText("");
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    getRiskIncidence();
    BrowserService.Util.getInstance().modifyRiskIncidence(incidence, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        errorMessage(caught);
      }

      @Override
      public void onSuccess(Void result) {
        Tools.newHistory(RiskIncidenceRegister.RESOLVER, ShowRiskIncidence.RESOLVER.getHistoryToken(),
          incidence.getId());
      }

    });
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    Tools.newHistory(RiskIncidenceRegister.RESOLVER);
  }

  private void errorMessage(Throwable caught) {
    if (caught instanceof NotFoundException) {
      Toast.showError(messages.editIncidenceNotFound(incidence.getId()));
      cancel();
    } else {
      Toast.showError(messages.editIncidenceFailure(caught.getMessage()));
    }
  }

}
