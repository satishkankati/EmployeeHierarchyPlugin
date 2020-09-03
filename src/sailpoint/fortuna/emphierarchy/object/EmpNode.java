package sailpoint.fortuna.emphierarchy.object;
import java.util.ArrayList;
import java.util.List;
 
public class EmpNode {
 private String id;
 private String name;
 private final List<EmpNode> children = new ArrayList<EmpNode>();
 private EmpNode parent;
 
 public EmpNode getParent() {
	return parent;
}
 public void setParent(EmpNode parent) {
	this.parent = parent;
}
 public String getId() {
  return id;
 }
 
 public void setId(String id) {
  this.id = id;
 }
 
 public String getName() {
	return name;
}
 public void setName(String name) {
	this.name = name;
}
 
 public List<EmpNode> getChildren() {
  return children;
 }

}