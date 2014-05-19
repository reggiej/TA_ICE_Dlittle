package com.demo.app.web;

import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;


import com.icesoft.faces.component.tree.IceUserObject;

public class TreeBean {
	
	private DefaultTreeModel model;
		
	public TreeBean(){
		
		
	}
	
	/*public void addTechNodes ( List<Technology> techList){
		DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode();
		IceUserObject rootObject = new IceUserObject(rootTreeNode);
		rootObject.setExpanded(true);
		rootTreeNode.setUserObject(rootObject);
		
		setModel(new DefaultTreeModel(rootTreeNode));
		rootObject.setText("Technology");
		for (int i =0; i< techList.size(); i++){
			Technology techData = new Technology();
			techData = techList.get(i);
			DefaultMutableTreeNode branchNode = new DefaultMutableTreeNode();
			IceUserObject branchObject = new IceUserObject(branchNode);
			branchObject.setText(techData.getTechName());
			branchObject.setRowIndex(techData.getTechID().intValue());
			branchObject.setLeaf(false);
			branchNode.setAllowsChildren(false);
			branchNode.setUserObject(branchObject);
			rootTreeNode.add(branchNode);
		}	
	}
		
	public void addEntityNodes (List<Entity> entityList){
		DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode();
		IceUserObject rootObject = new IceUserObject(rootTreeNode);
		rootObject.setExpanded(true);
		rootTreeNode.setUserObject(rootObject);
		
		setModel(new DefaultTreeModel(rootTreeNode));
		rootObject.setText("Entity");
		for (int i=0; i<entityList.size(); i++){
			Entity entityData = new Entity();
			entityData = entityList.get(i);
			DefaultMutableTreeNode branchNode = new DefaultMutableTreeNode();
			IceUserObject branchObject = new IceUserObject(branchNode);
			branchObject.setText(entityData.getEntityName());
			branchObject.setRowIndex(entityData.getEntityID().intValue());
			branchNode.setAllowsChildren(false);
			branchNode.setUserObject(branchObject);
			rootTreeNode.add(branchNode);
		}
	}
	
	public void addStateNodes (List<State> stateList){
		DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode();
		IceUserObject rootObject = new IceUserObject(rootTreeNode);
		rootObject.setExpanded(true);
		rootTreeNode.setUserObject(rootObject);
		
		setModel(new DefaultTreeModel(rootTreeNode));
		rootObject.setText("State");
		for (int i=0; i<stateList.size(); i++){
			State stateData = new State();
			stateData = stateList.get(i);
			DefaultMutableTreeNode branchNode = new DefaultMutableTreeNode();
			IceUserObject branchObject = new IceUserObject(branchNode);
			branchObject.setText(stateData.getStateName());
			branchObject.setRowIndex(stateData.getStateID().intValue());
			branchNode.setAllowsChildren(false);
			branchNode.setUserObject(branchObject);
			rootTreeNode.add(branchNode);
		}
	}
	
	public void addCountryNodes (List<Countries> countryList){
		DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode();
		IceUserObject rootObject = new IceUserObject(rootTreeNode);
		rootObject.setExpanded(true);
		rootTreeNode.setUserObject(rootObject);
		
		setModel(new DefaultTreeModel(rootTreeNode));
		rootObject.setText("Country");
		for (int i=0; i<countryList.size(); i++){
			Countries countryData = new Countries();
			countryData = countryList.get(i);
			DefaultMutableTreeNode branchNode = new DefaultMutableTreeNode();
			IceUserObject branchObject = new IceUserObject(branchNode);
			branchObject.setText(countryData.getCountryName());
			branchObject.setRowIndex(countryData.getCountryID().intValue());
			branchNode.setAllowsChildren(false);
			branchNode.setUserObject(branchObject);
			rootTreeNode.add(branchNode);
		}
	}
	
	public void selectTreeNode(ActionEvent dez){
		//dez.
	}

	public void setModel(DefaultTreeModel model) {
		this.model = model;
	}

	public DefaultTreeModel getModel() {
		return model;
	}
	
	
*/
}
