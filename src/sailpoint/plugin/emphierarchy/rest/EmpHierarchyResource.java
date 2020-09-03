package sailpoint.plugin.emphierarchy.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.SailPointContext;
import sailpoint.object.Filter;
import sailpoint.object.Identity;
import sailpoint.object.QueryOptions;
import sailpoint.plugin.emphierarchy.server.EmpNode;
import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.RequiredRight;
import sailpoint.tools.GeneralException;

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
		System.out.println("Constructor: EmpHierarchyResource");;
			attrName= getSettingString("attrName");
	}

	/**
	 * Returns the employee hierarchy json.
	 * 
	 * @return
	 * @throws Exception
	 *             if managers are in infinite loop
	 */
	@RequiredRight(value = "EmpHierarchyAccessRestServiceAllow")
	@POST
	@Path("getEmployees")
	@Produces(MediaType.APPLICATION_JSON)
	public List getEmployeeHierarchyJson(@FormParam("attrValue") String attrValue,
			@FormParam("manager") String managerName) throws GeneralException {
		log.info("EmpHierarchyResource:getEmployeeHierarchyJson Method invoked");
		EmpNode node = null;
		log.info("EmpHierarchyResource:getEmployeeHierarchyJson attributes recd. attrValue :" + attrValue);
		log.info("EmpHierarchyResource:getEmployeeHierarchyJson attributes recd. managerName :" + managerName);
		node = getEmployees(attrValue, managerName);
		return node.getChildren();
	}

	/**
	 * Generate the employee hierarchy tree.
	 * 
	 * @return
	 * @throws GeneralException
	 */
	private EmpNode getEmployees(String attrValue, String managerName) throws GeneralException {
		log.info("EmpHierarchyResource:getEmployees attributes recd. attrValue :" + attrValue);
		log.info("EmpHierarchyResource:getEmployees attributes recd. managerName :" + managerName);
		SailPointContext context = getContext();
		QueryOptions q = new QueryOptions();
		List<Identity> identities;
		// Creating root node
		EmpNode root = new EmpNode();
		root.setName(null);
		root.setId(null);

		Queue<Identity> queue = new LinkedList<Identity>();
		Queue<Identity> queue1 = new LinkedList<Identity>();
		List<String> added = new ArrayList<String>();
		boolean flag = true;

		try {
			if (attrValue != null)
				if (!attrValue.equals("All")) {
					flag = false;
					q.add(Filter.eq(attrName, attrValue));
				}
			if (managerName != null && !managerName.isEmpty()) {
				flag = false;
				q.add(Filter.or(Filter.eq("manager.name", managerName), Filter.eq("name", managerName)));
			}
			if (flag)
				q.add(Filter.or(Filter.isnull("manager"), Filter.eq("managerStatus", true)));

			q.add(Filter.eq("workgroup", false));
			identities = context.getObjects(Identity.class, q);

			for (Identity idy : identities) {
				Identity manager = idy.getManager();
				EmpNode parent = findParentNode(root, manager == null ? null : manager.getId());
				if (manager != null && parent == null) {
					if (!attrValue.equals("All")
							&& !manager.getAttribute(attrName).equals(attrValue)
							&& !added.contains(idy.getId())) {
						addChild(root, idy.getName(), idy.getId());
						added.add(idy.getId());
						List subOrds = getSubOrdinates(idy.getId(),attrName, attrValue);
						if (subOrds != null && subOrds.size() > 0)
							queue.addAll(subOrds);
					} else if (idy.getName().equals(managerName) && !added.contains(idy.getId())) {
						added.add(idy.getId());
						addChild(root, idy.getName(), idy.getId());
						List subOrds = getSubOrdinates(idy.getId(), attrName, attrValue);
						if (subOrds != null && subOrds.size() > 0)
							queue.addAll(subOrds);
					} else
						queue.add(idy);
				} else if (!added.contains(idy.getId())) {
					addChild(parent, idy.getName(), idy.getId());
					added.add(idy.getId());
					List subOrds = getSubOrdinates(idy.getId(), attrName, attrValue);
					if (subOrds != null && subOrds.size() > 0)
						queue.addAll(subOrds);

				}
			}
		} catch (GeneralException e) {
			throw e;
		}
		int size = 0;
		int count = 0; // used to terminate after 100 iterations if managers are
						// infinite loop
		boolean exist = true;
		log.info("EmpHierarchyResource:getEmployees processing second iteration ");
		do {
			exist = false;
			count++;
			queue1.clear();
			size = queue.size();
			while (!queue.isEmpty()) {
				Identity idy = queue.poll();
				Identity manager = idy.getManager();
				EmpNode parent = findParentNode(root, manager == null ? null : manager.getId());
				if (manager != null && parent == null) {
					if (!attrValue.equals("All")
							&& !manager.getAttribute(attrName).equals(attrValue)
							&& !added.contains(idy.getId())) {
						addChild(root, idy.getName(), idy.getId());
						added.add(idy.getId());
						List subOrds = getSubOrdinates(idy.getId(), attrName, attrValue);
						if (subOrds != null && subOrds.size() > 0)
							queue1.addAll(subOrds);
					} else if (idy.getName().equals(managerName) && !added.contains(idy.getId())) {
						added.add(idy.getId());
						addChild(root, idy.getName(), idy.getId());
						List subOrds = getSubOrdinates(idy.getId(), attrName, attrValue);
						if (subOrds != null && subOrds.size() > 0)
							queue1.addAll(subOrds);
					} else
						queue1.add(idy);
				} else if (!added.contains(idy.getId())) {
					exist = true;
					log.info("EmpHierarchyResource:getEmployees Adding to Employee to Queue ::" + idy.getName());
					addChild(parent, idy.getName(), idy.getId());
					added.add(idy.getId());
					List subOrds = getSubOrdinates(idy.getId(), attrName, attrValue);
					if (subOrds != null && subOrds.size() > 0)
						queue1.addAll(subOrds);
				} else
					log.info("EmpHierarchyResource:getEmployees Already added to Queue :: " + idy.getName());
			}
			log.info("EmpHierarchyResource:getEmployees counter :: " + count);

			if (size > 0 && size == queue1.size() && !exist || count > 99) {
				log.info("EmpHierarchyResource:getEmployees queue size :: " + size
						+ "Infinite Loop........................Queue 1 size :: " + queue1.size());
				log.info("EmpHierarchyResource:getEmployees Queue :: " + queue1);

				throw new GeneralException(findLoopManagers(queue1));
			} else
				queue.addAll(queue1);
		} while (!queue1.isEmpty());
		return root;
	}

	/**
	 * get the loop of managers
	 * 
	 * @return
	 * @throws GeneralException
	 */
	private String findLoopManagers(Queue<Identity> queue1) {
		List list = new LinkedList<>();
		String returnString = "";
		boolean found = false;
		Identity idy = null;
		List<String> visited = new ArrayList<String>();
		if (queue1 != null) {
			idy = queue1.poll();
			visited.add(idy.getId());
			list.add(idy.getName());
		}
		while (!queue1.isEmpty() && !found) {
			log.info("EmpHierarchyResource:findLoopManagers identity ::" + idy.getName());
			idy = idy.getManager();
			queue1.remove(idy);
			if (visited.contains(idy.getId())) {
				found = true;
			} else {
				visited.add(idy.getId());
				list.add(idy.getName());
			}

		}
		if (found) {
			for (int i = visited.indexOf(idy.getId()); i < visited.size(); i++)
				returnString = returnString + " --> " + list.get(i);
			returnString = returnString + " --> " + list.get(visited.indexOf(idy.getId()));
			log.info("EmpHierarchyResource:findLoopManagers loop :: " + returnString);
			return returnString.substring(5);
		} else
			return null;
	}

	/**
	 * get the subordinates
	 * 
	 * @return
	 * @throws GeneralException
	 */
	private List getSubOrdinates(String id, String attrName, String attrValue) throws GeneralException {
		SailPointContext context = getContext();
		QueryOptions q = new QueryOptions();
		if (attrValue != null)
			if (!attrValue.equals("All")) {
				q.add(Filter.eq(attrName, attrValue));
			}
		q.add(Filter.eq("workgroup", false));
		q.add(Filter.eq("manager.id", id));
		List<Identity> identities = context.getObjects(Identity.class, q);
		return identities;
	}

	/**
	 * Adding employee to hierarchy
	 * 
	 * @return
	 */
	private EmpNode addChild(EmpNode parent, String empName, String id) {
		EmpNode node = new EmpNode();
		node.setId(id);
		node.setName(empName);
		parent.getChildren().add(node);
		return node;
	}

	/**
	 * find the parent node of employee
	 * 
	 * @return
	 * @throws GeneralException
	 */
	private static EmpNode findParentNode(EmpNode rootNode, String mgrId) {
		Stack<EmpNode> stack = new Stack<EmpNode>();
		List<String> visited = new ArrayList<String>();
		if (mgrId == null)
			return rootNode;

		stack.add(rootNode);
		visited.add(rootNode.getId());
		while (!stack.isEmpty()) {

			EmpNode node = stack.pop();
			if (node.getId() != null && node.getId().equals(mgrId))
				return node;
			List<EmpNode> children = node.getChildren();
			for (int i = 0; i < children.size(); i++) {
				EmpNode n = children.get(i);
				if (n != null && !visited.contains(n.getId())) {
					stack.add(n);
					visited.add(n.getId());
				}
			}
		}
		return null;
	}

	/**
	 * Returns Dropdown values of configured attribute.
	 * 
	 * @return
	 * @throws Exception
	 *             if Attribute not found.
	 */
	@RequiredRight(value =  "EmpHierarchyAccessRestServiceAllow" )
	@GET
	@Path("getAttributeValues")
	public Map getAttributeValues() throws GeneralException {
		Map<String, Object> map = new HashMap<String, Object>();
		List<String> values = new ArrayList();
		try {
			values.add("All");
			SailPointContext context = getContext();
			QueryOptions q = new QueryOptions();
			q.add(Filter.notnull(attrName));
			q.setDistinct(true);
			List prop = new ArrayList();
		
			prop.add(attrName);
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>"+q);
			Iterator iter = context.search(Identity.class, q, prop);
			while (iter.hasNext()) {
				Object[] row = (Object[]) iter.next();
				values.add(row[0].toString());
			}
			map.put("ATTRNAME", attrName);
			map.put("ATTRVALUES", values);
			map.put("MANAGERS", getManagers());
			return map;
		} catch (GeneralException ex) {
			ex.printStackTrace();
			throw ex;
		}
		catch(RuntimeException re){
			re.printStackTrace();
			throw new GeneralException(re.getMessage());
		}

	}

	private List getManagers() throws GeneralException {
		SailPointContext context = getContext();
		QueryOptions q = new QueryOptions();
		q.setDistinct(true);
		List prop = new ArrayList();
		prop.add("name");
		prop.add("displayName");
		List<Map> values = new ArrayList();
		Map m = new HashMap();
		m.put("key", "");
		m.put("value", "All");
		values.add(m);
		Iterator iter = context.search(Identity.class, q, prop);
		while (iter.hasNext()) {
			Object[] row = (Object[]) iter.next();
			m = new HashMap();
			m.put("key", row[0].toString());
			m.put("value", row[1] == null ? row[0].toString() : row[1].toString());
			values.add(m);
		}
		return values;
	}

	@Override
	public String getPluginName() {
		// TODO Auto-generated method stub
		return "emphierarchy";
	}
}
