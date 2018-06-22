/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.client.ingest.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.lists.IngestJobReportList;
import org.roda.wui.client.common.lists.SimpleJobReportList;
import org.roda.wui.client.common.lists.pagination.ListSelectionUtils;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.common.search.SearchPreFilterUtils;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.client.ingest.appraisal.IngestAppraisal;
import org.roda.wui.client.process.ActionProcess;
import org.roda.wui.client.process.IngestProcess;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.client.process.Process;
import org.roda.wui.client.search.Search;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.FacetUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.Humanize.DHMSFormat;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class ShowJob extends Composite {

  private static final int PERIOD_MILLIS = 10000;

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        String jobId = historyTokens.get(0);
        BrowserService.Util.getInstance().retrieveJobBundle(jobId, new ArrayList<>(),
          new AsyncCallback<JobBundle>() {

            @Override
            public void onFailure(Throwable caught) {
              if (caught instanceof NotFoundException) {
                Toast.showError(messages.notFoundError(), messages.jobNotFound());
                HistoryUtils.newHistory(Process.RESOLVER);
              } else {
                AsyncCallbackUtils.defaultFailureTreatment(caught);
              }
            }

            @Override
            public void onSuccess(JobBundle jobBundle) {
              Map<String, PluginInfo> pluginsInfo = new HashMap<>();
              for (PluginInfo pluginInfo : jobBundle.getPluginsInfo()) {
                pluginsInfo.put(pluginInfo.getId(), pluginInfo);
              }

              ShowJob showJob = new ShowJob(jobBundle.getJob(), pluginsInfo);
              callback.onSuccess(showJob);
            }

          });
      } else if (historyTokens.size() > 1 && historyTokens.get(0).equals(ShowJobReport.RESOLVER.getHistoryToken())) {
        ShowJobReport.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else {
        HistoryUtils.newHistory(Process.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      // TODO check for show job permission
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {Process.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Process.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "job";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, ShowJob> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  // empty to get all job information
  private static final List<String> fieldsToReturn = new ArrayList<>();

  private static final List<String> aipFieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_LEVEL,
    RodaConstants.AIP_TITLE);

  private Job job;
  private final Map<String, PluginInfo> pluginsInfo;

  @UiField
  Label name;

  @UiField
  Label creator;

  @UiField
  Label dateStarted;

  @UiField
  Label dateEndedLabel, dateEnded;

  @UiField
  Label duration;

  @UiField
  HTML progress;

  @UiField
  HTML status;

  @UiField
  Label stateDetailsLabel, stateDetailsValue;

  @UiField
  FlowPanel selectedListPanel;

  @UiField
  FlowPanel selectedList;

  @UiField
  Label plugin;

  @UiField
  FlowPanel pluginPanel;

  @UiField
  FlowPanel pluginOptions;

  @UiField
  FlowPanel reportListPanel;

  @UiField
  Label reportsLabel;

  @UiField(provided = true)
  SearchPanel ingestJobReportsSearchPanel;

  @UiField(provided = true)
  IngestJobReportList ingestJobReports;

  @UiField(provided = true)
  SearchPanel simpleJobReportsSearchPanel;

  @UiField(provided = true)
  SimpleJobReportList simpleJobReports;

  @UiField
  Button buttonAppraisal, buttonBack, buttonStop, buttonProcess;

  @UiField(provided = true)
  FlowPanel facetsPanel;

  public ShowJob(Job job, Map<String, PluginInfo> pluginsInfo) {
    this.job = job;
    this.pluginsInfo = pluginsInfo;
    boolean isIngest = false;

    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.JOB_REPORT_JOB_ID, job.getUUID()));

    if (job.getPluginType().equals(PluginType.INGEST)) {
      ingestJobReports = new IngestJobReportList("ShowJob_reports",
        new Filter(new SimpleFilterParameter(RodaConstants.JOB_REPORT_JOB_ID, job.getId())), messages.reportList(),
        pluginsInfo, false);
      ListSelectionUtils.bindBrowseOpener(ingestJobReports);
      simpleJobReports = new SimpleJobReportList("ShowJob_reports_hidden");
      isIngest = true;
    } else {
      simpleJobReports = new SimpleJobReportList("ShowJob_reports",
        new Filter(new SimpleFilterParameter(RodaConstants.JOB_REPORT_JOB_ID, job.getId())), messages.reportList(),
        pluginsInfo, false);
      ListSelectionUtils.bindBrowseOpener(simpleJobReports);
      ingestJobReports = new IngestJobReportList("ShowJob_reports_hidden");
    }

    ingestJobReportsSearchPanel = new SearchPanel(filter, RodaConstants.JOB_REPORT_SEARCH, true,
      messages.jobProcessedSearchPlaceHolder(), false, false, false);
    ingestJobReportsSearchPanel.setList(ingestJobReports);

    simpleJobReportsSearchPanel = new SearchPanel(filter, RodaConstants.JOB_REPORT_SEARCH, true,
      messages.jobProcessedSearchPlaceHolder(), false, false, false);
    simpleJobReportsSearchPanel.setList(simpleJobReports);

    facetsPanel = new FlowPanel();

    initWidget(uiBinder.createAndBindUi(this));
    simpleJobReportsSearchPanel.setVisible(!isIngest);
    simpleJobReports.setVisible(!isIngest);
    ingestJobReportsSearchPanel.setVisible(isIngest);
    ingestJobReports.setVisible(isIngest);
    buttonProcess.setVisible(isIngest);

    name.setText(job.getName());
    creator.setText(job.getUsername());
    dateStarted.setText(Humanize.formatDateTime(job.getStartDate()));
    update();

    SelectedItems<?> selected = job.getSourceObjects();
    selectedListPanel.setVisible(true);

    if (isIngest) {
      FacetUtils.bindFacets(ingestJobReports, facetsPanel);

      if (isJobRunning()) {
        ingestJobReports.autoUpdate(PERIOD_MILLIS);
      }

      ingestJobReports.getSelectionModel().addSelectionChangeHandler(new Handler() {
        @Override
        public void onSelectionChange(SelectionChangeEvent event) {
          Report jobReport = ingestJobReports.getSelectionModel().getSelectedObject();
          if (jobReport != null) {
            HistoryUtils.newHistory(ShowJobReport.RESOLVER, jobReport.getId());
          }
        }
      });

      showIngestSourceObjects(selected);
    } else {
      FacetUtils.bindFacets(ingestJobReports, facetsPanel);

      if (isJobRunning()) {
        simpleJobReports.autoUpdate(PERIOD_MILLIS);
      }

      simpleJobReports.getSelectionModel().addSelectionChangeHandler(new Handler() {
        @Override
        public void onSelectionChange(SelectionChangeEvent event) {
          Report jobReport = simpleJobReports.getSelectionModel().getSelectedObject();
          if (jobReport != null) {
            HistoryUtils.newHistory(ShowJobReport.RESOLVER, jobReport.getId());
          }
        }
      });

      showActionSourceObjects(selected);
    }

    PluginInfo pluginInfo = pluginsInfo.get(job.getPlugin());
    if (pluginInfo != null) {
      plugin.setText(messages.pluginLabelWithVersion(pluginInfo.getName(), pluginInfo.getVersion()));

      if (pluginInfo.getParameters().isEmpty()) {
        pluginPanel.setVisible(false);
        pluginOptions.setVisible(false);
      } else {
        pluginPanel.setVisible(true);
        pluginOptions.setVisible(true);
      }

      for (PluginParameter parameter : pluginInfo.getParameters()) {

        if (PluginParameterType.BOOLEAN.equals(parameter.getType())) {
          createBooleanLayout(parameter);
        } else if (PluginParameterType.STRING.equals(parameter.getType())) {
          createStringLayout(parameter);
        } else if (PluginParameterType.PLUGIN_SIP_TO_AIP.equals(parameter.getType())) {
          createPluginSipToAipLayout(parameter);
        } else if (PluginParameterType.AIP_ID.equals(parameter.getType())) {
          createSelectAipLayout(parameter);
        } else {
          createStringLayout(parameter);
        }
      }
    } else {
      plugin.setText(job.getPlugin());
      pluginPanel.setVisible(false);
      pluginOptions.setVisible(false);
    }
  }

  @Override
  protected void onDetach() {
    if (autoUpdateTimer != null) {
      autoUpdateTimer.cancel();
    }
    super.onDetach();
  }

  @Override
  protected void onLoad() {
    if (autoUpdateTimer != null && !autoUpdateTimer.isRunning() && isJobRunning()) {
      autoUpdateTimer.scheduleRepeating(PERIOD_MILLIS);
    }

    JavascriptUtils.stickSidebar();
    super.onLoad();
  }

  private boolean isJobRunning() {
    return job != null && !job.isInFinalState();
  }

  private boolean isJobInFinalState() {
    return job != null && job.isInFinalState();
  }

  private void showIngestSourceObjects(final SelectedItems<?> selected) {
    if (selected != null) {
      selectedList.clear();
      selectedListPanel.setVisible(true);

      if (ClientSelectedItemsUtils.isEmpty(selected) && isJobInFinalState()) {
        Label noSourceLabel = new Label(messages.noItemsToDisplay());
        selectedListPanel.add(noSourceLabel);
      } else if (selected instanceof SelectedItemsList) {
        List<String> ids = ((SelectedItemsList<?>) selected).getIds();
        final Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, ids));
        BrowserService.Util.getInstance().count(selected.getSelectedClass(), filter, true, new AsyncCallback<Long>() {
          @Override
          public void onFailure(Throwable caught) {
            Label noSourceLabel = new Label(messages.noItemsToDisplay());
            selectedListPanel.add(noSourceLabel);
          }

          @Override
          public void onSuccess(Long result) {
            InlineHTML filterHTML = new InlineHTML(messages.sourceObjectList(result, messages.someOfAObject(selected.getSelectedClass())));
            selectedList.add(filterHTML);

            Button button = new Button(messages.downloadButton());
            button.addStyleName("btn btn-separator-left btn-link");
            button.addClickHandler(new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                BrowserService.Util.getInstance().getExportLimit(new AsyncCallback<Integer>() {

                  @Override
                  public void onFailure(Throwable caught) {
                    AsyncCallbackUtils.defaultFailureTreatment(caught);
                  }

                  @Override
                  public void onSuccess(Integer result) {
                    Toast.showInfo(messages.exportListTitle(), messages.exportListMessage(result));
                    RestUtils.requestCSVExport(selected.getSelectedClass(), filter, Sorter.NONE,
                            new Sublist(0, result.intValue()), Facets.NONE, true, false, "source_objects.csv");
                  }
                });
              }
            });
            selectedList.add(button);
          }
        });
      } else if (selected instanceof SelectedItemsFilter) {
        Filter filter = ((SelectedItemsFilter<?>) selected).getFilter();
        HTML filterHTML = new HTML(
          SearchPreFilterUtils.getFilterHTML(filter, selected.getSelectedClass()));
        selectedList.add(filterHTML);
      } else if (selected instanceof SelectedItemsAll) {
        Label filterLabel = new Label(messages.allOfAObject(selected.getSelectedClass()));
        filterLabel.addStyleName("value");
        selectedList.add(filterLabel);
      } else {
        selectedListPanel.setVisible(false);
      }
    }
  }

  private void showActionSourceObjects(final SelectedItems<?> selected) {
    if (selected != null) {
      selectedList.clear();

      if (selected instanceof SelectedItemsList) {
        List<String> ids = ((SelectedItemsList<?>) selected).getIds();

        if (ids.isEmpty()) {
          Label noSourceLabel = new Label(messages.noItemsToDisplay());
          selectedListPanel.add(noSourceLabel);
        } else {
          final Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, ids));
          BrowserService.Util.getInstance().count(selected.getSelectedClass(), filter, true, new AsyncCallback<Long>() {
            @Override
            public void onFailure(Throwable caught) {
              Label noSourceLabel = new Label(messages.noItemsToDisplay());
              selectedListPanel.add(noSourceLabel);
            }

            @Override
            public void onSuccess(Long result) {
              InlineHTML filterHTML = new InlineHTML(messages.sourceObjectList(result, messages.someOfAObject(selected.getSelectedClass())));
              selectedList.add(filterHTML);

              Button button = new Button(messages.downloadButton());
              button.addStyleName("btn btn-separator-left btn-link");
              button.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                  BrowserService.Util.getInstance().getExportLimit(new AsyncCallback<Integer>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      AsyncCallbackUtils.defaultFailureTreatment(caught);
                    }

                    @Override
                    public void onSuccess(Integer result) {
                      Toast.showInfo(messages.exportListTitle(), messages.exportListMessage(result));
                      RestUtils.requestCSVExport(selected.getSelectedClass(), filter, Sorter.NONE,
                        new Sublist(0, result.intValue()), Facets.NONE, true, false, "source_objects.csv");
                    }
                  });
                }
              });
              selectedList.add(button);
            }
          });
        }
      } else if (selected instanceof SelectedItemsFilter) {
        Filter filter = ((SelectedItemsFilter<?>) selected).getFilter();
        HTML filterHTML = new HTML(SearchPreFilterUtils.getFilterHTML(filter, selected.getSelectedClass()));
        selectedList.add(filterHTML);
      } else if (selected instanceof SelectedItemsAll || selected instanceof SelectedItemsNone) {
        Label objectLabel = new Label();
        objectLabel.addStyleName("value");

        if (StringUtils.isBlank(selected.getSelectedClass())) {
          objectLabel.setText(messages.noItemsToDisplay());
        } else if (AIP.class.getName().equals(selected.getSelectedClass())
          || IndexedAIP.class.getName().equals(selected.getSelectedClass())) {
          objectLabel.setText(messages.allIntellectualEntities());
        } else if (Representation.class.getName().equals(selected.getSelectedClass())
          || IndexedRepresentation.class.getName().equals(selected.getSelectedClass())) {
          objectLabel.setText(messages.allRepresentations());
        } else if (File.class.getName().equals(selected.getSelectedClass())
          || IndexedFile.class.getName().equals(selected.getSelectedClass())) {
          objectLabel.setText(messages.allFiles());
        } else {
          objectLabel.setText(messages.allOfAObject(selected.getSelectedClass()));
        }

        selectedList.add(objectLabel);
      } else {
        selectedListPanel.setVisible(false);
      }
    }
  }

  private void update() {
    // set end date
    dateEndedLabel.setVisible(job.getEndDate() != null);
    dateEnded.setVisible(job.getEndDate() != null);
    if (job.getEndDate() != null) {
      dateEnded.setText(Humanize.formatDateTime(job.getEndDate()));
    }

    // set duration
    duration.setText(Humanize.durationInDHMS(job.getStartDate(), job.getEndDate(), DHMSFormat.LONG));

    // set state
    status.setHTML(HtmlSnippetUtils.getJobStateHtml(job));

    // set state details
    boolean hasStateDetails = StringUtils.isNotBlank(job.getStateDetails());
    stateDetailsLabel.setVisible(hasStateDetails);
    stateDetailsValue.setVisible(hasStateDetails);
    if (hasStateDetails) {
      stateDetailsValue.setText(job.getStateDetails());
    }

    // set counters
    SafeHtmlBuilder b = new SafeHtmlBuilder();
    b.append(SafeHtmlUtils.fromSafeConstant("<span class='label-default'>"));
    b.append(messages.showJobProgressCompletionPercentage(job.getJobStats().getCompletionPercentage()));
    b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
    if (job.getJobStats().getSourceObjectsCount() > 0) {
      b.append(SafeHtmlUtils.fromSafeConstant("&nbsp;<span class='label-default'>"));
      b.append(messages.showJobProgressTotalCount(job.getJobStats().getSourceObjectsCount()));
      b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
    }
    if (job.getJobStats().getSourceObjectsProcessedWithSuccess() > 0) {
      b.append(SafeHtmlUtils.fromSafeConstant("&nbsp;<span class='label-success'>"));
      b.append(messages.showJobProgressSuccessfulCount(job.getJobStats().getSourceObjectsProcessedWithSuccess()));
      b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
    }
    if (job.getJobStats().getSourceObjectsProcessedWithFailure() > 0) {
      b.append(SafeHtmlUtils.fromSafeConstant("&nbsp;<span class='label-danger'>"));
      b.append(messages.showJobProgressFailedCount(job.getJobStats().getSourceObjectsProcessedWithFailure()));
      b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
    }
    if (job.getJobStats().getSourceObjectsBeingProcessed() > 0) {
      b.append(SafeHtmlUtils.fromSafeConstant("&nbsp;<span class='label-info'>"));
      b.append(messages.showJobProgressProcessingCount(job.getJobStats().getSourceObjectsBeingProcessed()));
      b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
    }
    if (job.getJobStats().getSourceObjectsWaitingToBeProcessed() > 0) {
      b.append(SafeHtmlUtils.fromSafeConstant("&nbsp;<span class='label-warning'>"));
      b.append(messages.showJobProgressWaitingCount(job.getJobStats().getSourceObjectsWaitingToBeProcessed()));
      b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
    }

    progress.setHTML(b.toSafeHtml());

    buttonStop.setText(messages.stopButton());
    buttonStop.setVisible(!job.isInFinalState());
    buttonStop.setEnabled(!job.isStopping());

    buttonAppraisal
      .setText(messages.appraisalTitle() + " (" + job.getJobStats().getOutcomeObjectsWithManualIntervention() + ")");
    buttonAppraisal.setVisible(job.getJobStats().getOutcomeObjectsWithManualIntervention() > 0);

    scheduleUpdateStatus();
  }

  private Timer autoUpdateTimer = null;

  private void scheduleUpdateStatus() {
    if (!job.isInFinalState()) {
      if (autoUpdateTimer == null) {
        autoUpdateTimer = new Timer() {

          @Override
          public void run() {
            BrowserService.Util.getInstance().retrieve(Job.class.getName(), job.getId(), fieldsToReturn,
              new AsyncCallback<Job>() {

                @Override
                public void onFailure(Throwable caught) {
                  AsyncCallbackUtils.defaultFailureTreatment(caught);
                }

                @Override
                public void onSuccess(Job updatedJob) {
                  ShowJob.this.job = updatedJob;
                  update();
                  scheduleUpdateStatus();
                }
              });
          }
        };
      }
      autoUpdateTimer.schedule(PERIOD_MILLIS);
    }
  }

  private void createSelectAipLayout(PluginParameter parameter) {
    Label parameterName = new Label(parameter.getName());
    final FlowPanel aipPanel = new FlowPanel();
    final String value = job.getPluginParameters().containsKey(parameter.getId())
      ? job.getPluginParameters().get(parameter.getId()) : parameter.getDefaultValue();

    if (value != null && !value.isEmpty()) {
      BrowserService.Util.getInstance().retrieve(IndexedAIP.class.getName(), value, aipFieldsToReturn,
        new AsyncCallback<IndexedAIP>() {

          @Override
          public void onFailure(Throwable caught) {
            if (caught instanceof NotFoundException) {
              Label itemTitle = new Label(value);
              itemTitle.addStyleName("itemText");
              aipPanel.clear();
              aipPanel.add(itemTitle);
            } else {
              AsyncCallbackUtils.defaultFailureTreatment(caught);
            }
          }

          @Override
          public void onSuccess(IndexedAIP aip) {
            Label itemTitle = new Label();
            HTMLPanel itemIconHtmlPanel = DescriptionLevelUtils.getElementLevelIconHTMLPanel(aip.getLevel());
            itemIconHtmlPanel.addStyleName("itemIcon");
            itemTitle.setText(aip.getTitle() != null ? aip.getTitle() : aip.getId());
            itemTitle.addStyleName("itemText");

            aipPanel.clear();
            aipPanel.add(itemIconHtmlPanel);
            aipPanel.add(itemTitle);
          }
        });
    } else {
      HTMLPanel itemIconHtmlPanel = DescriptionLevelUtils.getTopIconHTMLPanel();
      aipPanel.clear();
      aipPanel.add(itemIconHtmlPanel);
    }

    pluginOptions.add(parameterName);
    pluginOptions.add(aipPanel);

    parameterName.addStyleName("form-label itemLabel");
    aipPanel.addStyleName("itemPanel itemPanelShow");
  }

  private void createBooleanLayout(PluginParameter parameter) {
    CheckBox checkBox = new CheckBox(parameter.getName());
    String value = job.getPluginParameters().get(parameter.getId());
    if (value == null) {
      value = parameter.getDefaultValue();
    }
    checkBox.setValue("true".equals(value));
    checkBox.setEnabled(false);

    pluginOptions.add(checkBox);
    addHelp(parameter.getDescription());

    checkBox.addStyleName("form-checkbox");
  }

  private void createStringLayout(PluginParameter parameter) {
    String value = job.getPluginParameters().get(parameter.getId());
    if (value == null) {
      value = parameter.getDefaultValue();
    }
    if (value != null && value.length() > 0) {
      Label parameterLabel = new Label(parameter.getName());
      Label parameterValue = new Label(value);
      pluginOptions.add(parameterLabel);
      pluginOptions.add(parameterValue);

      parameterLabel.addStyleName("label");

      addHelp(parameter.getDescription());
    }
  }

  private void createPluginSipToAipLayout(PluginParameter parameter) {
    String value = job.getPluginParameters().get(parameter.getId());

    if (value == null) {
      value = parameter.getDefaultValue();
    }

    if (StringUtils.isNotBlank(value)) {
      Label pluginLabel = new Label(parameter.getName());
      PluginInfo sipToAipPlugin = pluginsInfo.get(value);
      RadioButton pluginValue;

      pluginOptions.add(pluginLabel);
      addHelp(parameter.getDescription());

      if (sipToAipPlugin != null) {
        pluginValue = new RadioButton(parameter.getId(),
          messages.pluginLabelWithVersion(sipToAipPlugin.getName(), sipToAipPlugin.getVersion()));

        pluginValue.setValue(true);
        pluginValue.setEnabled(false);
        pluginOptions.add(pluginValue);
        addHelp(sipToAipPlugin.getDescription());

      } else {
        pluginValue = new RadioButton(parameter.getId(), value);
        pluginValue.setValue(true);
        pluginValue.setEnabled(false);
        pluginOptions.add(pluginValue);
      }

      pluginLabel.addStyleName("label");
      pluginValue.addStyleName("form-radiobutton");
    }
  }

  private void addHelp(String description) {
    if (description != null && description.length() > 0) {
      Label pHelp = new Label(description);
      pluginOptions.add(pHelp);
      pHelp.addStyleName("form-help");
    }
  }

  @UiHandler("buttonAppraisal")
  void buttonAppraisalHandler(ClickEvent e) {
    HistoryUtils.newHistory(IngestAppraisal.RESOLVER, RodaConstants.SEARCH_ITEMS, RodaConstants.INGEST_JOB_ID,
      job.getId());
  }

  @UiHandler("buttonBack")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  @UiHandler("buttonStop")
  void buttonStopHandler(ClickEvent e) {
    stop();
  }

  private void cancel() {
    if (job.getPluginType().equals(PluginType.INGEST)) {
      HistoryUtils.newHistory(IngestProcess.RESOLVER);
    } else if (job.getPluginType().equals(PluginType.INTERNAL)) {
      HistoryUtils.newHistory(InternalProcess.RESOLVER);
    } else {
      HistoryUtils.newHistory(ActionProcess.RESOLVER);
    }
  }

  private void stop() {
    Dialogs.showConfirmDialog(messages.jobStopConfirmDialogTitle(), messages.jobStopConfirmDialogMessage(),
      messages.dialogCancel(), messages.dialogYes(), new AsyncCallback<Boolean>() {

        @Override
        public void onFailure(Throwable caught) {
          // nothing to do
        }

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            BrowserService.Util.getInstance().stopJob(job.getId(), new AsyncCallback<Void>() {
              @Override
              public void onFailure(Throwable caught) {
                // FIXME 20160826 hsilva: do proper handling of the failure
              }

              @Override
              public void onSuccess(Void result) {
                // FIXME 20160826 hsilva: do proper handling of the success
              }
            });
          }
        }
      });
  }

  @UiHandler("buttonProcess")
  void buttonProcessHandler(ClickEvent e) {
    if (job != null) {
      HistoryUtils.newHistory(Search.RESOLVER, "items", RodaConstants.ALL_INGEST_JOB_IDS, job.getId());
    }
  }
}
