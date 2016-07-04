package com.axelor.studio.service.builder;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.meta.db.MetaView;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.meta.schema.views.AbstractWidget;
import com.axelor.meta.schema.views.Dashboard;
import com.axelor.meta.schema.views.Dashlet;
import com.axelor.studio.db.DashletBuilder;
import com.axelor.studio.db.ViewBuilder;

/**
 * This service class used to generate dashboard from ViewBuilder of type
 * Dashboard. It will take all selected charts and generate xml from it.
 * 
 * @author axelor
 *
 */
public class DashboardBuilderService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private List<ActionView> actions;

	/**
	 * Method to generate Dashboard (meta schema) from View Builder.
	 * 
	 * @param viewBuilder
	 *            ViewBuilder of type dashboard.
	 * @return Dashboard.
	 */
	public Dashboard getView(ViewBuilder dashboardBuilder) {

		log.debug("Processing dashboard: {}", dashboardBuilder.getName());

		Dashboard dashboard = new Dashboard();
		String boardName = dashboardBuilder.getName();
		dashboard.setTitle(dashboardBuilder.getTitle());
		dashboard.setName(dashboardBuilder.getName());
		List<AbstractWidget> dashlets = new ArrayList<AbstractWidget>();
		actions = new ArrayList<ActionView>();

		for (DashletBuilder dashletBuilder : dashboardBuilder
				.getDashletBuilderList()) {
			
			Dashlet dashlet = new Dashlet();
			String name = null;
			String model = null;
			ViewBuilder viewBuilder = dashletBuilder.getViewBuilder();
			MetaView metaView = dashletBuilder.getMetaView();
			
			if (viewBuilder != null) {
				name = viewBuilder.getName();
				model = viewBuilder.getModel();
			} else if (metaView != null) {
				name = metaView.getName();
				model = metaView.getModel();
			}

			String actionName = getAction(boardName, name, model,
					dashletBuilder);
			dashlet.setAction(actionName);
			dashlet.setHeight("350");
			
			Integer colSpan = dashletBuilder.getColspan();
			if (colSpan > 12) {
				colSpan = 12;
			} else if (colSpan <= 0) {
				colSpan = 6;
			}
			
			dashlet.setColSpan(colSpan);
			dashlets.add(dashlet);
		}

		dashboard.setItems(dashlets);

		return dashboard;
	}

	/**
	 * Metod to get actions-views for charts.
	 * 
	 * @return List of action-views.
	 */
	public List<ActionView> getActions() {

		List<ActionView> actionViews = actions;

		actions = null;

		return actionViews;
	}

	/**
	 * Method to generate action-view for a chart
	 * 
	 * @param dashboard
	 *            Dashboard in which chart to be used.
	 * @param chart
	 *            Chart to open from action-view.
	 * @return Name of action-view.
	 */
	private String getAction(String dashboard, String name, String model,
			DashletBuilder dashletBuilder) {

		ActionViewBuilder builder = ActionView.define(dashletBuilder.getName());
		String actionName = "action-"
				+ (dashboard + "-" + name).replace(".", "-");
		log.debug("Action name: {}", actionName);
		builder.name(actionName);
		builder.model(model);
		builder.add(dashletBuilder.getViewType(), name);
		builder.param("limit", dashletBuilder.getPaginationLimit().toString());
		actions.add(builder.get());

		return actionName;
	}
}
