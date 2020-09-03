package sailpoint.fortuna.emphierarchy.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.SailPointContext;
import sailpoint.fortuna.emphierarchy.object.EmpNode;
import sailpoint.object.Filter;
import sailpoint.object.Identity;
import sailpoint.object.QueryOptions;
import sailpoint.tools.GeneralException;

public class EmpUtil {
	private static Log log = LogFactory.getLog(EmpUtil.class);

	/**
	 * Generate the employee hierarchy tree.
	 * 
	 * @return
	 * @throws GeneralException
	 */
	public static EmpNode getEmployees(SailPointContext context, String attrName, String attrValue, String managerName)
			throws GeneralException {
		log.debug("EmpHierarchyResource:getEmployees attributes recd. attrValue :" + attrValue);
		log.debug("EmpHierarchyResource:getEmployees attributes recd. managerName :" + managerName);

		List<Map<String, String>> identities = null;

		// Creating root node
		EmpNode root = new EmpNode();
		root.setName(null);
		root.setId(null);

		Queue<Map<String, String>> queue = new LinkedList<Map<String, String>>();
		Queue<Map<String, String>> queue1 = new LinkedList<Map<String, String>>();
		List<String> added = new ArrayList<String>();
		boolean flag = true;

		QueryOptions q = new QueryOptions();
		try {
			if (attrValue != null)
				if (!attrValue.equals("All")) {
					flag = false;
					q.add(Filter.eq(attrName, attrValue));
				}
			if (managerName != null && !managerName.isEmpty() && !"All".equalsIgnoreCase(managerName)) {
				flag = false;
				q.add(Filter.or(Filter.eq("manager.name", managerName), Filter.eq("name", managerName)));
			}
			if (flag)
				q.add(Filter.or(Filter.isnull("manager"), Filter.eq("managerStatus", true)));

			q.add(Filter.eq("workgroup", false));
			identities = new ArrayList<Map<String, String>>();

			List<String> prop = new ArrayList<String>();
			prop.add("name");
			prop.add("id");
			Iterator<Object[]> iter = context.search(Identity.class, q, prop);
			while (iter.hasNext()) {
				Object[] row = (Object[]) iter.next();
				Map<String, String> m = new HashMap<String, String>();
				m.put("name", (String) row[0]);
				m.put("id", (String) row[1]);
				identities.add(m);
			}

			for (Map<String, String> idy : identities) {
				Identity identity = context.getObjectById(Identity.class, idy.get("id"));
				Identity manager = identity.getManager();
				EmpNode parent = findParentNode(root, manager == null ? null : manager.getId());
				if (manager != null && parent == null) {
					if (!attrValue.equals("All") && !manager.getAttribute(attrName).equals(attrValue)
							&& !added.contains(idy.get("id"))) {
						addChild(root, idy);
						added.add(idy.get("id"));
						List<Map<String, String>> subOrds = getSubOrdinates(context, idy.get("id"), attrName,
								attrValue);
						if (subOrds != null && subOrds.size() > 0)
							queue.addAll(subOrds);
					} else if (idy.get("name").equals(managerName) && !added.contains(idy.get("id"))) {
						added.add(idy.get("id"));
						addChild(root, idy);
						List<Map<String, String>> subOrds = getSubOrdinates(context, idy.get("id"), attrName,
								attrValue);
						if (subOrds != null && subOrds.size() > 0)
							queue.addAll(subOrds);
					} else
						queue.add(idy);
				} else if (!added.contains(idy.get("id"))) {
					addChild(parent, idy);
					added.add(idy.get("id"));
					List<Map<String, String>> subOrds = getSubOrdinates(context, idy.get("id"), attrName, attrValue);
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
		log.debug("EmpHierarchyResource:getEmployees processing second iteration ");
		do {
			exist = false;
			count++;
			queue1.clear();
			size = queue.size();
			while (!queue.isEmpty()) {
				Map<String, String> idy = queue.poll();
				Identity identity = context.getObjectById(Identity.class, idy.get("id"));
				Identity manager = identity.getManager();
				EmpNode parent = findParentNode(root, manager == null ? null : manager.getId());
				if (manager != null && parent == null) {
					if (!attrValue.equals("All") && !manager.getAttribute(attrName).equals(attrValue)
							&& !added.contains(idy.get("id"))) {
						addChild(root, idy);
						added.add(idy.get("id"));
						List<Map<String, String>> subOrds = getSubOrdinates(context, idy.get("id"), attrName,
								attrValue);
						if (subOrds != null && subOrds.size() > 0)
							queue1.addAll(subOrds);
					} else if (idy.get("name").equals(managerName) && !added.contains(idy.get("id"))) {
						added.add(idy.get("id"));
						addChild(root, idy);
						List<Map<String, String>> subOrds = getSubOrdinates(context, idy.get("id"), attrName,
								attrValue);
						if (subOrds != null && subOrds.size() > 0)
							queue1.addAll(subOrds);
					} else
						queue1.add(idy);
				} else if (!added.contains(idy.get("id"))) {
					exist = true;
					log.debug("EmpHierarchyResource:getEmployees Adding to Employee to Queue ::" + idy.get("name"));
					addChild(parent, idy);
					added.add(idy.get("id"));
					List<Map<String, String>> subOrds = getSubOrdinates(context, idy.get("id"), attrName, attrValue);
					if (subOrds != null && subOrds.size() > 0)
						queue1.addAll(subOrds);
				} else
					log.debug("EmpHierarchyResource:getEmployees Already added to Queue :: " + idy.get("name"));
			}
			log.debug("EmpHierarchyResource:getEmployees counter :: " + count);

			if (size > 0 && size == queue1.size() && !exist || count > 99) {
				log.debug("EmpHierarchyResource:getEmployees queue size :: " + size
						+ "Infinite Loop........................Queue 1 size :: " + queue1.size());
				log.debug("EmpHierarchyResource:getEmployees Queue :: " + queue1);

				throw new GeneralException(findLoopManagers(context, queue1));
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
	private static String findLoopManagers(SailPointContext context, Queue<Map<String, String>> queue1)
			throws GeneralException {
		List<String> list = new LinkedList<>();
		String returnString = "";
		boolean found = false;
		Map<String, String> idy = null;
		List<String> visited = new ArrayList<String>();
		if (queue1 != null) {
			idy = queue1.poll();
			visited.add(idy.get("id"));
			list.add(idy.get("name"));
		}
		while (!queue1.isEmpty() && !found) {
			log.debug("EmpHierarchyResource:findLoopManagers identity ::" + idy.get("name"));
			Identity identity = context.getObjectById(Identity.class, idy.get("id"));
			queue1.remove(idy);
			if (visited.contains(identity.getId())) {
				found = true;
			} else {
				visited.add(identity.getId());
				list.add(identity.getName());
			}

		}
		if (found) {
			for (int i = visited.indexOf(idy.get("id")); i < visited.size(); i++)
				returnString = returnString + " --> " + list.get(i);
			returnString = returnString + " --> " + list.get(visited.indexOf(idy.get("id")));
			log.debug("EmpHierarchyResource:findLoopManagers loop :: " + returnString);
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
	private static List<Map<String, String>> getSubOrdinates(SailPointContext context, String id, String attrName,
			String attrValue) throws GeneralException {
		QueryOptions q = new QueryOptions();
		if (attrValue != null)
			if (!attrValue.equals("All")) {
				q.add(Filter.eq(attrName, attrValue));
			}
		q.add(Filter.eq("workgroup", false));
		q.add(Filter.eq("manager.id", id));

		List<Map<String, String>> identities = new ArrayList<Map<String, String>>();

		List<String> prop = new ArrayList<String>();
		prop.add("name");
		prop.add("id");
		Iterator<Object[]> iter = context.search(Identity.class, q, prop);
		while (iter.hasNext()) {
			Object[] row = (Object[]) iter.next();
			Map<String, String> m = new HashMap<String, String>();
			m.put("name", (String) row[0]);
			m.put("id", (String) row[1]);
			identities.add(m);
		}
		return identities;
	}

	/**
	 * Adding employee to hierarchy
	 * 
	 * @return
	 */
	private static EmpNode addChild(EmpNode parent, Map<String, String> child) {
		EmpNode node = new EmpNode();
		node.setId(child.get("id"));
		node.setName(child.get("name"));
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

	public static String initCaps(String str) {
		String cap = str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
		return cap;
	}

}
