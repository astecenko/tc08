package ru.rostvertolplc.osapr.tc08.handlers;

import org.eclipse.jface.window.Window;

import java.util.ArrayList;
import java.util.Vector;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.AIFClipboard;
import com.teamcenter.rac.aif.AIFPortal;
import com.teamcenter.rac.aif.AIFTransferable;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentPseudoFolder;
import com.teamcenter.rac.util.MessageBox;

import ru.rostvertolplc.osapr.helpers.*;
import ru.rostvertolplc.osapr.tc08.components.SelectDialog;

public class MainHandler extends AbstractHandler {

	public static final String sNotEqual = "!";
	public static final String sDelimiter = ";";
	public static final String sOtherVariant = "+";
	public static final String sAllVariant = "*";

	public static final String sPrefTypes = "RVT_TC08_TYPES";
	public static final String sPrefRelations = "RVT_TC08_RELATIONS";
	public static final String sPrefCaptions = "RVT_TC08_CAPTIONS";

	public static final String sDefPropMaterial = "H47_RelationMaterial";
	public static final String sDefType = "+H47_Standart_Izd Revision";

	private Vector<TCComponent> localVector;

	private boolean allItem;
	private boolean allRelation;
	private boolean isItemRev;

	private ArrayList<String> itemEqual;
	private ArrayList<String> itemNotEqual;
	private ArrayList<String> itemRevisionEqual;
	private ArrayList<String> itemRevisionNotEqual;
	private ArrayList<String> relationEqual;
	private ArrayList<String> relationNotEqual;
	private ArrayList<String> referencEqual;
	private ArrayList<String> referencNotEqual;

	public MainHandler() {
	}

	void checkComponent2(InterfaceAIFComponent comp1) {		
		try {
			if ((comp1 instanceof TCComponentFolder)
					|| ((comp1 instanceof TCComponentPseudoFolder) && (comp1.toString().equals("View"))))
				for (AIFComponentContext interAIFCompCont1 : comp1.getChildren())
					checkComponent2(interAIFCompCont1.getComponent());
			else {
				if (comp1 instanceof TCComponentItem)
					checkComponent(comp1);
				else if (comp1 instanceof TCComponentItemRevision)
					checkComponent(((TCComponentItemRevision) comp1).getItem());

				if ((comp1 instanceof TCComponentItem)
						|| (comp1 instanceof TCComponentItemRevision))
					for (AIFComponentContext interAIFCompCont1 : comp1
							.getChildren()) {
						checkComponent2(interAIFCompCont1.getComponent());
					}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	void checkComponent(InterfaceAIFComponent comp1) {
		if ((comp1 instanceof TCComponentItem)
				|| (comp1 instanceof TCComponentItemRevision)) {

			boolean itemexst;
			boolean relatexst;
			// boolean isItemRev = false;
			// boolean allItem, allRelation;
			// allItem = false;
			// allRelation = false;

			TCComponentItem item1 = null;
			TCComponentItemRevision itemRev1 = null;
			AIFComponentContext[] arrayOfAIFCompContext1;
			if (comp1 instanceof TCComponentItemRevision)
				try {
					item1 = ((TCComponentItemRevision) comp1).getItem();
				} catch (Exception e) {
					// TODO: handle exception
				}
			else
				item1 = (TCComponentItem) comp1;

			try {
				itemRev1 = item1.getLatestItemRevision();
			} catch (Exception e) {
				// TODO: handle exception
			}
			if (allItem) // если все типы то отмечаем что булем проходить по
				// item
				itemexst = true;
			else {
				itemexst = !((itemEqual.size() > 0) || (itemRevisionEqual
						.size() > 0));
				isItemRev = false;
				if ((!itemEqual.isEmpty()) && (!itemexst)) {
					for (String str1 : itemEqual) {
						if (item1.getType().equals(str1)) {
							itemexst = true;
							break;
						}
					}
				}

				if ((itemRev1 != null) && (!itemRevisionEqual.isEmpty())
						&& (!itemexst)) {
					for (String str1 : itemRevisionEqual) {
						if (itemRev1.getType().equals(str1)) {
							itemexst = true;
							isItemRev = true;
							break;
						}
					}
				}

				if ((!itemNotEqual.isEmpty()) && (itemexst)) {
					for (String str1 : itemNotEqual) {
						if (item1.getType().equals(str1)) {
							itemexst = false;
							break;
						}
					}
				}

				if ((itemRev1 != null) && (!itemRevisionNotEqual.isEmpty())
						&& (itemexst)) {
					for (String str1 : itemRevisionNotEqual) {
						if (itemRev1.getType().equals(str1)) {
							itemexst = false;
							break;
						}
					}
				}
			}
			// если тут itemexst то провер€ем св€зи
			if ((itemexst) && (!localVector.contains(item1))) {
				if (allRelation)
					relatexst = true;
				else {
					relatexst = !((relationEqual.size() > 0) || (referencEqual
							.size() > 0));

					if ((itemRev1 != null) && (isItemRev)) { // провер€ем item
						// revision
						if ((!relationEqual.isEmpty()) && (!relatexst)) {
							for (String str1 : relationEqual) {
								try {
									if (itemRev1.getRelatedComponent(str1) != null) {
										relatexst = true;
										break;
									}
								} catch (Exception e) {
									// TODO: handle exception
								}

							}
						}
						try {
							if ((itemRev1 != null)
									&& (!referencEqual.isEmpty())
									&& (!relatexst)
									&& (itemRev1.getWhereReferencedCount() > 0)) {
								for (String str1 : referencEqual) {
									arrayOfAIFCompContext1 = itemRev1
											.whereReferenced();
									for (AIFComponentContext compContext1 : arrayOfAIFCompContext1) {
										if (compContext1
												.getContextDisplayName()
												.equals(str1)) {
											relatexst = true;
											break;
										}
									}
								}
							}

							if ((!relationNotEqual.isEmpty()) && (relatexst)) {
								for (String str1 : relationNotEqual) {
									if (itemRev1.getRelatedComponent(str1) != null) {
										relatexst = false;
										break;
									}
								}
							}

							if ((!referencNotEqual.isEmpty()) && (relatexst)
									&& (itemRev1.getWhereReferencedCount() > 0)) {
								for (String str1 : referencNotEqual) {
									arrayOfAIFCompContext1 = itemRev1
											.whereReferenced();
									for (AIFComponentContext compContext1 : arrayOfAIFCompContext1) {
										if (compContext1
												.getContextDisplayName()
												.equals(str1)) {
											relatexst = false;
											break;
										}
									}
								}
							}
						} catch (Exception e) {
							// TODO: handle exception
						}

					} else { // провер€ем item
						try {
							if ((!relationEqual.isEmpty()) && (!relatexst)) {
								for (String str1 : relationEqual) {
									if (item1.getRelatedComponent(str1) != null) {
										relatexst = true;
										break;
									}
								}
							}

							if ((!referencEqual.isEmpty()) && (!relatexst)
									&& (item1.getWhereReferencedCount() > 0)) {
								for (String str1 : referencEqual) {
									arrayOfAIFCompContext1 = item1
											.whereReferenced();
									for (AIFComponentContext compContext1 : arrayOfAIFCompContext1) {
										if (compContext1
												.getContextDisplayName()
												.equals(str1)) {
											relatexst = true;
											break;
										}
									}
								}
							}

							if ((!referencNotEqual.isEmpty()) && (relatexst)) {
								for (String str1 : relationNotEqual) {
									if (item1.getRelatedComponent(str1) != null) {
										relatexst = false;
										break;
									}
								}
							}

							if ((!referencNotEqual.isEmpty()) && (relatexst)
									&& (item1.getWhereReferencedCount() > 0)) {
								for (String str1 : referencNotEqual) {
									arrayOfAIFCompContext1 = item1
											.whereReferenced();
									for (AIFComponentContext compContext1 : arrayOfAIFCompContext1) {
										if (compContext1
												.getContextDisplayName()
												.equals(str1)) {
											relatexst = false;
											break;
										}
									}
								}
							}
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
				}
				if (relatexst)
					localVector.addElement(item1);
			}
		}
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		localVector = new Vector<TCComponent>();
		InterfaceAIFComponent[] c_targets = AIFUtility.getTargetComponents();
		IWorkbenchWindow window = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);

		String[] listTypes = PreferenceHelper
				.getPreferenceValueArray(sPrefTypes);
		String[] listRelations = PreferenceHelper
				.getPreferenceValueArray(sPrefRelations);
		String[] listCaptions = PreferenceHelper
				.getPreferenceValueArray(sPrefCaptions);
		if ((listTypes == null) || listTypes.length == 0)
			listTypes = new String[] { sDefType };
		if ((listRelations == null) || listRelations.length == 0)
			listRelations = new String[] { sDefPropMaterial };
		if ((listCaptions == null) || listCaptions.length == 0)
			listCaptions = new String[] { "—тандартные издели€ без материала" };

		SelectDialog myDialog = new SelectDialog(window.getShell(),
				"ѕоиск и копирование", "", listCaptions,
				IMessageProvider.INFORMATION);
		int retVal = myDialog.open();
		if (retVal == Window.OK) {
			retVal = myDialog.getReturnCode();
			itemEqual = new ArrayList<String>();
			itemNotEqual = new ArrayList<String>();
			itemRevisionEqual = new ArrayList<String>();
			itemRevisionNotEqual = new ArrayList<String>();
			relationEqual = new ArrayList<String>();
			relationNotEqual = new ArrayList<String>();
			referencEqual = new ArrayList<String>();
			referencNotEqual = new ArrayList<String>();

			// boolean itemexst;
			// boolean relatexst;
			isItemRev = false;
			allItem = false;
			allRelation = false;

			// TCComponentItem item1;
			// TCComponentItemRevision itemRev1;
			// AIFComponentContext[] arrayOfAIFCompContext1;

			listTypes = listTypes[retVal].split(sDelimiter);
			listRelations = listRelations[retVal].split(sDelimiter);

			// разбираем типы
			for (String str : listTypes) {
				if (str.startsWith(sNotEqual)) {
					if (str.startsWith(sOtherVariant, 1))
						itemRevisionNotEqual.add(str.substring(2));
					else
						itemNotEqual.add(str.substring(1));
				} else {
					if (str.startsWith(sAllVariant)) {
						allItem = true;
						if (str.startsWith(sOtherVariant, 1)) {
							isItemRev = true;
							itemRevisionEqual.add(str);
						}
						itemEqual.add(str);
					} else if (str.startsWith(sOtherVariant))
						itemRevisionEqual.add(str.substring(1));
					else
						itemEqual.add(str);
				}
			}

			// разбираем св€зи
			for (String str : listRelations) {
				if (str.startsWith(sNotEqual)) {
					if (str.startsWith(sOtherVariant, 1))
						referencNotEqual.add(str.substring(2));
					else
						relationNotEqual.add(str.substring(1));

				} else {
					if (str.startsWith(sAllVariant)) {
						allRelation = true;
						relationEqual.add(sAllVariant);
					} else {
						if (str.startsWith(sOtherVariant))
							referencEqual.add(str.substring(1));
						else
							relationEqual.add(str);

					}
				}
			}

			if (c_targets != null) {
				for (InterfaceAIFComponent interfaceAIFComponent : c_targets) {
					try {
						checkComponent2(interfaceAIFComponent);
						/*
						 * if (interfaceAIFComponent instanceof
						 * TCComponentFolder) { for (AIFComponentContext
						 * interAIFCompCont1 : ((TCComponentFolder)
						 * interfaceAIFComponent) .getChildren()) {
						 * 
						 * checkComponent(interAIFCompCont1.getComponent()); } }
						 * else if (interfaceAIFComponent instanceof
						 * TCComponentItem)
						 * checkComponent(interfaceAIFComponent); else if
						 * (interfaceAIFComponent instanceof
						 * TCComponentItemRevision)
						 * checkComponent(((TCComponentItemRevision)
						 * interfaceAIFComponent) .getItem());
						 */

						/*
						 * if (interAIFCompCont1.getComponent() instanceof
						 * TCComponentItem) {
						 * 
						 * item1 = (TCComponentItem) interAIFCompCont1
						 * .getComponent(); itemRev1 =
						 * item1.getLatestItemRevision(); if (allItem) itemexst
						 * = true; else { itemexst = !((itemEqual.size() > 0) ||
						 * (itemRevisionEqual .size() > 0)); isItemRev = false;
						 * 
						 * if ((!itemEqual.isEmpty()) && (!itemexst)) { for
						 * (String str1 : itemEqual) { if (item1.getType()
						 * .equals(str1)) { itemexst = true; break; } } }
						 * 
						 * if ((!itemRevisionEqual.isEmpty()) && (!itemexst)) {
						 * for (String str1 : itemRevisionEqual) { if
						 * (itemRev1.getType().equals( str1)) { itemexst = true;
						 * isItemRev = true; break; } } }
						 * 
						 * if ((!itemNotEqual.isEmpty()) && (itemexst)) { for
						 * (String str1 : itemNotEqual) { if (item1.getType()
						 * .equals(str1)) { itemexst = false; break; } } }
						 * 
						 * if ((!itemRevisionNotEqual.isEmpty()) && (itemexst))
						 * { for (String str1 : itemRevisionNotEqual) { if
						 * (itemRev1.getType().equals( str1)) { itemexst =
						 * false; break; } } } } // если тут itemexst то
						 * провер€ем св€зи if (itemexst) { if (allRelation)
						 * relatexst = true; else { relatexst =
						 * !((relationEqual.size() > 0) || (referencEqual
						 * .size() > 0));
						 * 
						 * if (isItemRev) { // провер€ем item // revision if
						 * ((!relationEqual.isEmpty()) && (!relatexst)) { for
						 * (String str1 : relationEqual) { if (itemRev1
						 * .getRelatedComponent(str1) != null) { relatexst =
						 * true; break; } } }
						 * 
						 * if ((!referencEqual.isEmpty()) && (!relatexst) &&
						 * (itemRev1 .getWhereReferencedCount() > 0)) { for
						 * (String str1 : referencEqual) {
						 * arrayOfAIFCompContext1 = itemRev1 .whereReferenced();
						 * for (AIFComponentContext compContext1 :
						 * arrayOfAIFCompContext1) { if (compContext1
						 * .getContextDisplayName() .equals( str1)) { relatexst
						 * = true; break; } } } }
						 * 
						 * if ((!relationNotEqual .isEmpty()) && (relatexst)) {
						 * for (String str1 : relationNotEqual) { if (itemRev1
						 * .getRelatedComponent(str1) != null) { relatexst =
						 * false; break; } } }
						 * 
						 * if ((!referencNotEqual .isEmpty()) && (relatexst) &&
						 * (itemRev1 .getWhereReferencedCount() > 0)) { for
						 * (String str1 : referencNotEqual) {
						 * arrayOfAIFCompContext1 = itemRev1 .whereReferenced();
						 * for (AIFComponentContext compContext1 :
						 * arrayOfAIFCompContext1) { if (compContext1
						 * .getContextDisplayName() .equals( str1)) { relatexst
						 * = false; break; } } } } } else { // провер€ем item if
						 * ((!relationEqual.isEmpty()) && (!relatexst)) { for
						 * (String str1 : relationEqual) { if (item1
						 * .getRelatedComponent(str1) != null) { relatexst =
						 * true; break; } } }
						 * 
						 * if ((!referencEqual.isEmpty()) && (!relatexst) &&
						 * (item1 .getWhereReferencedCount() > 0)) { for (String
						 * str1 : referencEqual) { arrayOfAIFCompContext1 =
						 * item1 .whereReferenced(); for (AIFComponentContext
						 * compContext1 : arrayOfAIFCompContext1) { if
						 * (compContext1 .getContextDisplayName() .equals(
						 * str1)) { relatexst = true; break; } } } }
						 * 
						 * if ((!referencNotEqual .isEmpty()) && (relatexst)) {
						 * for (String str1 : relationNotEqual) { if (item1
						 * .getRelatedComponent(str1) != null) { relatexst =
						 * false; break; } } }
						 * 
						 * if ((!referencNotEqual .isEmpty()) && (relatexst) &&
						 * (item1 .getWhereReferencedCount() > 0)) { for (String
						 * str1 : referencNotEqual) { arrayOfAIFCompContext1 =
						 * item1 .whereReferenced(); for (AIFComponentContext
						 * compContext1 : arrayOfAIFCompContext1) { if
						 * (compContext1 .getContextDisplayName() .equals(
						 * str1)) { relatexst = false; break; } } } } } } if
						 * (relatexst) localVector.addElement(item1); } }
						 */

					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			}
			if (!localVector.isEmpty()) {

				AIFClipboard localAIFClipboard = AIFPortal.getClipboard();
				AIFTransferable localAIFTransferable = new AIFTransferable(
						localVector);
				localAIFClipboard.setContents(localAIFTransferable, null);
				
				MessageBox.post("ќбнаружено соответствующих условию поиска  и скопировано, объектов: " + Integer.toString(localVector.size()), "Teamcenter",
						MessageBox.INFORMATION);			
			} else
				MessageBox.post("ќбъектов соответствующих условию поиска не обнаружено!", "Teamcenter",
						MessageBox.INFORMATION);
		}
		return null;
	}
}