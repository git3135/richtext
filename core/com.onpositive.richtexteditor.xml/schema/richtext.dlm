<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<namespace description="" name="richtext" url="http://jetface.org/JetFace1.0/richtext">
    <element allowsChilds="actions" class="com.onpositive.richtexteditor.xml.RichtextElementHandler" description="" extends="http://jetface.org/JetFace1.0/editor" group="controls" name="richtext">
        <property description="" name="richTextConfigurator" type="java"/>
        <property description="" name="formatFactoryClass" type="java"/>
        <property description="" name="useExternalToolbar" type="boolean"/>
    </element>
    <element allowsChilds="customAction,boldAction,italicAction,underlineAction,strikethroughAction,fontStyleAction,addImageAction,addHRAction,openFileAction" class="com.onpositive.richtexteditor.xml.RichtextElementHandler" description="" extends="" name="actions"/>
    <element allowsChilds="" class="com.onpositive.richtexteditor.xml.RichtextElementHandler" description="" extends="" name="customAction">
        <property description="" name="className" required="true" type="java"/>
    </element>
    <element allowsChilds="" class="com.onpositive.richtexteditor.xml.RichtextElementHandler" description="" extends="" name="boldAction"/>
    <element allowsChilds="" class="com.onpositive.richtexteditor.xml.RichtextElementHandler" description="" extends="" name="italicAction">
        <property description="" name="231" required="true" type="222"/>
    </element>
    <element allowsChilds="" class="com.onpositive.richtexteditor.xml.RichtextElementHandler" description="" extends="" name="underlineAction"/>
    <element allowsChilds="" class="com.onpositive.richtexteditor.xml.RichtextElementHandler" description="" extends="" name="strikethroughAction"/>
    <element allowsChilds="" class="com.onpositive.richtexteditor.xml.RichtextElementHandler" description="" extends="" name="addImageAction"/>
    <element allowsChilds="" class="com.onpositive.richtexteditor.xml.RichtextElementHandler" description="" extends="" name="addHRAction"/>
    <element allowsChilds="" class="com.onpositive.richtexteditor.xml.RichtextElementHandler" description="" extends="" name="openFileAction"/>
    <element allowsChilds="" class="com.onpositive.richtexteditor.xml.RichtextElementHandler" description="" extends="" name="separator"/>
    <element allowsChilds="" class="com.onpositive.richtexteditor.xml.RichtextElementHandler" description="" extends="" name="bulletedListAction"/>
    <element allowsChilds="" class="com.onpositive.richtexteditor.xml.RichtextElementHandler" description="" extends="" name="numberedListAction"/>
    <element allowsChilds="" class="" description="" extends="" name="leftAlignAction"/>
    <element allowsChilds="" class="" description="" extends="" name="rightAlignAction"/>
    <element allowsChilds="" class="" description="" extends="" name="centerAlignAction"/>
    <element allowsChilds="" class="" description="" extends="" name="fitAlignAction"/>
    <element allowsChilds="" class="" description="" extends="" name="newLinkAction"/>
</namespace>
