module com.sciome.bmdexpress2
{

	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires javafx.base;

	requires javafx.swing;
	requires javafx.web;

	requires java.desktop;
	requires com.fasterxml.jackson.annotation;
	requires fontawesomefx;
	requires commons.math3;
	requires jfreechart;
	requires math;
	requires org.apache.commons.lang3;
	requires commons.cli;
	requires guava;
	requires jfreechart.fx;
	requires jcommon;
	requires commons.io;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires java.management;
	requires org.controlsfx.controls;

	opens com.sciome.bmdexpress2 to javafx.fxml;
	opens com.sciome.bmdexpress2.mvp.view.mainstage to javafx.fxml;
	opens com.sciome.bmdexpress2.mvp.view.prefilter to javafx.fxml;
	opens com.sciome.bmdexpress2.mvp.view.annotation to javafx.fxml;
	opens com.sciome.bmdexpress2.mvp.view.bmdanalysis to javafx.fxml;
	opens com.sciome.bmdexpress2.mvp.view.mainstage.dataview to javafx.fxml;
	opens com.sciome.bmdexpress2.mvp.view.visualization to javafx.fxml;
	opens com.sciome.bmdexpress2.mvp.view.categorization to javafx.fxml;
	opens com.sciome.bmdexpress2.util.bmds.shared to com.fasterxml.jackson.databind;
	opens com.toxicR.model to com.fasterxml.jackson.databind;
	opens com.sciome.bmdexpress2.mvp.model to com.fasterxml.jackson.databind;
	opens com.sciome.bmdexpress2.mvp.model.prefilter to com.fasterxml.jackson.databind;
	opens com.sciome.bmdexpress2.mvp.model.probe to com.fasterxml.jackson.databind;
	opens com.sciome.bmdexpress2.mvp.model.category to com.fasterxml.jackson.databind;
	opens com.sciome.bmdexpress2.mvp.model.category.identifier to com.fasterxml.jackson.databind;
	opens com.sciome.bmdexpress2.mvp.model.category.ivive to com.fasterxml.jackson.databind;
	opens com.sciome.bmdexpress2.mvp.model.chip to com.fasterxml.jackson.databind;
	opens com.sciome.bmdexpress2.mvp.model.info to com.fasterxml.jackson.databind;
	opens com.sciome.bmdexpress2.mvp.model.pca to com.fasterxml.jackson.databind;
	opens com.sciome.bmdexpress2.mvp.model.refgene to com.fasterxml.jackson.databind;
	opens com.sciome.bmdexpress2.mvp.model.stat to com.fasterxml.jackson.databind;
	opens com.sciome.filter to com.fasterxml.jackson.databind;

	opens com.sciome.bmdexpress2.mvp.presenter to guava;
	opens com.sciome.bmdexpress2.mvp.presenter.annotation to guava;
	opens com.sciome.bmdexpress2.mvp.presenter.prefilter to guava;
	opens com.sciome.bmdexpress2.mvp.presenter.bmdanalysis to guava;
	opens com.sciome.bmdexpress2.mvp.presenter.categorization to guava;
	opens com.sciome.bmdexpress2.mvp.presenter.visualization to guava;
	opens com.sciome.bmdexpress2.mvp.presenter.mainstage.dataview to guava;

	exports com.sciome.bmdexpress2;
	exports com.sciome.filter;
	exports com.sciome.bmdexpress2.mvp.view.mainstage;
	exports com.sciome.bmdexpress2.mvp.view;
	exports com.sciome.bmdexpress2.mvp.presenter.presenterbases;
	exports com.sciome.bmdexpress2.mvp.presenter.mainstage;
	exports com.sciome.bmdexpress2.mvp.presenter.visualization;
	exports com.sciome.bmdexpress2.mvp.presenter;
	exports com.sciome.bmdexpress2.mvp.model.prefilter;
	exports com.sciome.bmdexpress2.mvp.model;
	exports com.sciome.bmdexpress2.mvp.model.stat;
	exports com.sciome.bmdexpress2.mvp.model.category;
	exports com.sciome.bmdexpress2.mvp.model.category.identifier;
	exports com.sciome.bmdexpress2.mvp.view.visualization;
	exports com.sciome.bmdexpress2.util.bmds.shared;
	exports com.sciome.bmdexpress2.util.bmds;
	exports com.sciome.bmdexpress2.util;
	exports com.sciome.bmdexpress2.util.categoryanalysis;
	exports com.sciome.bmdexpress2.util.categoryanalysis.catmap;
	exports com.sciome.bmdexpress2.util.categoryanalysis.defined;
	exports com.sciome.bmdexpress2.util.prefilter;
	exports com.sciome.bmdexpress2.util.visualizations.curvefit;
	exports com.toxicR;
	exports com.toxicR.model;
	exports com.sciome.bmdexpress2.commandline.config;
	exports com.sciome.bmdexpress2.commandline.config.bmds;
	exports com.sciome.bmdexpress2.commandline.config.prefilter;
	exports com.sciome.bmdexpress2.commandline.config.expression;
	exports com.sciome.bmdexpress2.commandline.config.nonparametric;
	exports com.sciome.bmdexpress2.commandline.config.category;
	exports com.sciome.bmdexpress2.commandline;
}