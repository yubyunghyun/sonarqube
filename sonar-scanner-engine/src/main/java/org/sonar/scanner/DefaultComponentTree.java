package org.sonar.scanner;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;

import org.picocontainer.Startable;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.fs.InputModule;
import org.sonar.api.batch.fs.internal.DefaultInputModule;
import org.sonar.api.batch.fs.internal.InputComponentTree;
import org.sonar.scanner.scan.ImmutableProjectReactor;

public class DefaultComponentTree implements Startable, InputComponentTree {

  private ImmutableProjectReactor projectReactor;
  private TreeNode<InputModule> root;
  private Map<InputComponent, TreeNode<InputComponent>> index;

  public DefaultComponentTree(ImmutableProjectReactor projectReactor) {
    this.projectReactor = projectReactor;
  }

  @Override
  public void start() {
    doStart(projectReactor.getRoot());
  }

  private static void createChildren(ProjectDefinition parentDef, TreeNode<InputModule> parent) {
    for (ProjectDefinition def : parentDef.getSubProjects()) {
      InputModule childModule = new DefaultInputModule(def);
      TreeNode<InputModule> childNode = new TreeNode<>(childModule);
      childNode.setParent(parent.value());
      parent.addChild(childNode.value());
      createChildren(def, childNode);
    }
  }

  @Override
  public InputModule root() {
    return root.value();
  }

  @Override
  public Collection<InputComponent> getChildren(InputComponent component) {
    return index.get(component).children();
  }

  @Override
  public InputComponent getParent(InputComponent component) {
    return index.get(component).parent();
  }

  void doStart(ProjectDefinition rootProjectDefinition) {
    index = new HashMap<>();
    InputModule rootModule = new DefaultInputModule(rootProjectDefinition);
    root = new TreeNode<>(rootModule);

    createChildren(rootProjectDefinition, root);
  }

  private static class TreeNode<T> {
    private T t;
    private T parent;
    private List<T> children = new LinkedList<>();

    TreeNode(T t) {
      this.t = t;
    }

    T value() {
      return t;
    }

    @CheckForNull
    T parent() {
      return parent;
    }

    List<T> children() {
      return children;
    }

    void addChild(T child) {
      this.children.add(child);
    }

    void setParent(T parent) {
      this.parent = parent;
    }
  }

  @Override
  public void stop() {
    // nothing to do
  }
}
