import hudson.model.*;
import hudson.views.*;
import hudson.util.*;
import jenkins.model.*;

import java.util.logging.Level
import java.util.logging.Logger

import com.cloudbees.jce.masterprovisioning.view.MastersView;
import com.cloudbees.jce.masterprovisioning.view.column.*;
import com.cloudbees.jce.masterprovisioning.mesos.MasterConfigurationStaleViewColumn;

Logger logger = Logger.getLogger("init.initial-view.groovy")


def j = Jenkins.instance;
def vjp = j.getPlugin("view-job-filters");
if (vjp == null) {
    logger.log(Level.WARNING, 'view-job-filters plugin is not installed. Skipping view setup')
}

def masters = j.getView('Masters');
if (masters != null) {
    if (!(masters instanceof MastersView)) {
        j.deleteView(masters);
        masters = null;
    }
}
if (masters == null) {
    ListView mlv = new MastersView('controllers', j);
    mlv.setRecurse(true)
    DescribableList<ViewJobFilter, Descriptor<ViewJobFilter>> jf = mlv.getJobFilters();

    jf.add(new JobTypeFilter("com.cloudbees.opscenter.server.model.ClientMaster", "includeMatched"));
    jf.add(new JobTypeFilter("com.cloudbees.opscenter.server.model.ManagedMaster", "includeMatched"));
    jf.add(new SecurityFilter("MustMatchAll", false, true, false, "excludeUnmatched"));

    DescribableList<ListViewColumn, Descriptor<ListViewColumn>> cols = mlv.getColumns();

    cols.clear();
    cols.add(new StatusColumn());
    cols.add(new WeatherColumn());
    cols.add(new JobColumn());
    cols.add(new ManageMasterListViewColumn());
    cols.add(new com.cloudbees.opscenter.server.clusterops.adhoc.ListSelectionColumn());
    cols.add(new TotalJobsViewColumn());
    cols.add(new QueueSizeViewColumn());
    cols.add(new JenkinsVersionViewColumn());
    cols.add(new MasterConfigurationStaleViewColumn());

    j.addView(mlv);
    j.primaryView = mlv;
    j.save()
}

def cur = j.getView("all")
if (cur instanceof AllView) {
    def lv = new MastersView("All", j);
    lv.includeRegex = '.*'
    def cols = lv.columns;
    cols.clear();
    cols.add(new StatusColumn());
    cols.add(new WeatherColumn());
    cols.add(new JobColumn());
    cols.add(new ManageMasterListViewColumn());
    cols.add(new com.cloudbees.opscenter.server.clusterops.adhoc.ListSelectionColumn());
    j.deleteView(cur);
    j.addView(lv);
}
