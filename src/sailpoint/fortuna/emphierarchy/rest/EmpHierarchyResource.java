package sailpoint.fortuna.emphierarchy.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.JSONObject;

import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.fortuna.emphierarchy.object.EmpNode;
import sailpoint.fortuna.emphierarchy.util.EmpUtil;
import sailpoint.integration.ListResult;
import sailpoint.integration.RequestResult;
import sailpoint.object.Filter;
import sailpoint.object.Filter.MatchMode;
import sailpoint.object.Identity;
import sailpoint.object.QueryOptions;
import sailpoint.rest.plugin.AllowAll;
import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.RequiredRight;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;

/**
 * @author Satish Kumar Kankati Fortuna Identity
 * 
 */
@RequiredRight(value = "EmpHierarchyAccessRestServiceAllow")
@Path("emphierarchy")
public class EmpHierarchyResource extends BasePluginResource {
	private static Log log = LogFactory.getLog(EmpHierarchyResource.class);
	String attrName = null;

	/**
	 * Constructor
	 * 
	 */
	public EmpHierarchyResource() {
		super();
		log.debug("Constructor: EmpHierarchyResource");
		;
		attrName = getSettingString("attrName");
	}

	/**
	 * Returns the employee hierarchy json.
	 * 
	 * @return
	 * @throws Exception if managers are in infinite loop
	 */
	@RequiredRight(value = "EmpHierarchyAccessRestServiceAllow")
	@POST
	@Path("getEmployees")
	@Produces(MediaType.APPLICATION_JSON)
	public List<EmpNode> getEmployeeHierarchy(@FormDataParam("attrValue") String attrValue,
			@FormDataParam("manager") String managerName) throws GeneralException {
		log.debug("EmpHierarchyResource:getEmployeeHierarchyJson Method invoked");
		EmpNode node = null;
		log.debug("EmpHierarchyResource:getEmployeeHierarchyJson attributes recd. attrValue :" + attrValue);
		System.out
				.println("EmpHierarchyResource:getEmployeeHierarchyJson attributes recd. managerName :" + managerName);
		node = EmpUtil.getEmployees(getContext(), attrName, attrValue, managerName);
		return node.getChildren();
	}

	/**
	 * Returns Dropdown values of configured attribute.
	 * 
	 * @return
	 * @throws Exception if Attribute not found.
	 */
	@RequiredRight(value = "EmpHierarchyAccessRestServiceAllow")
	@GET
	@Path("getAttributeLabel")
	public Map<String, Object> getAttributeLabel() throws GeneralException {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ATTRNAME", EmpUtil.initCaps(getSettingString("attrName")));
		return map;
	}

	/**
	 * Retrieves a list of managed applications..
	 * 
	 * @param sFilter the header parameter JSON data in a string format.
	 * @return response containing the group data
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@GET
	@AllowAll
	@Path("/getAttributeValues")
	@Produces(MediaType.APPLICATION_JSON)
	public RequestResult getAttributeValues(@HeaderParam("params") String paramString) throws Exception {

		SailPointContext context = SailPointFactory.getCurrentContext();
		JSONObject paramJson = new JSONObject(paramString);
		log.debug("Inputs Obtained.. " + paramJson);
		List<Map> values = new ArrayList<Map>();
		int pageSize = paramJson.getInt("pageSize");
		int startIdx = 0;

		if (!paramJson.isNull("startIdx")) {
			startIdx = paramJson.getInt("startIdx");
		}
		QueryOptions ops = new QueryOptions();
		ops.add(Filter.notnull(attrName));
		ops.setDistinct(true);

		if (!paramJson.isNull("searchValue")) {
			String searchValue = paramJson.getString("searchValue");
			if (Util.isNotNullOrEmpty(searchValue))
				ops.add(Filter.like(attrName, searchValue, MatchMode.ANYWHERE));
		}
		List prop = new ArrayList();
		prop.add(attrName);
		int count = context.countObjects(Identity.class, ops);
		ops.setResultLimit(pageSize);
		ops.setFirstRow(startIdx);
		try {
			Map valueMap = new HashMap();
			valueMap.put("displayName", "All");
			valueMap.put("name", "All");
			valueMap.put("id", "All");
			values.add(valueMap);

			Iterator iter = context.search(Identity.class, ops, prop);
			while (iter.hasNext()) {
				Object[] row = (Object[]) iter.next();
				valueMap = new HashMap();
				valueMap.put("displayName", row[0]);
				valueMap.put("name", row[0]);
				valueMap.put("id", row[0]);
				values.add(valueMap);
			}

		} catch (GeneralException e) {

		} catch (Exception e) {

		}
		ListResult localListResult = new ListResult(values, count);

		return localListResult;
	}

	@Override
	public String getPluginName() {
		// TODO Auto-generated method stub
		return "emphierarchy";
	}
}
