package me.Cutiemango.MangoQuest.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.data.QuestPlayerData;
import me.Cutiemango.MangoQuest.questobjects.SimpleQuestObject;
import net.citizensnpcs.api.npc.NPC;

public class Quest {
	
	public Quest(String InternalID, String name, String QuestOutline, QuestReward reward, List<QuestStage> stages, NPC npc){
		this.InternalID = InternalID;
		this.QuestName = QuestUtil.translateColor(name);
		this.QuestOutline = QuestUtil.translateColor(QuestOutline);
		this.reward = reward;
		this.AllStages = stages;
		this.QuestNPC = npc;
		
		for (RequirementType t : RequirementType.values()){
			switch(t){
			case ITEM:
				Requirements.put(t, new ArrayList<ItemStack>());
				break;
			case LEVEL:
				Requirements.put(t, 0);
				break;
			case MONEY:
				Requirements.put(t, 0.0D);
				break;
			case NBTTAG:
				Requirements.put(t, new ArrayList<String>());
				break;
			case QUEST:
				Requirements.put(t, new ArrayList<String>());
				break;
			case SCOREBOARD:
				Requirements.put(t, new ArrayList<String>());
				break;
			}
		}
	}
	
	private NPC QuestNPC;
	private String InternalID;

	private String QuestName;
	private String QuestOutline;
	private String FailRequirementMessage;
	private List<QuestStage> AllStages = new ArrayList<>();
	private QuestReward reward;
	
	private EnumMap<RequirementType, Object> Requirements = new EnumMap<>(RequirementType.class);
	//private List<QuestRequirement> Requirements = new ArrayList<>();
	private List<QuestTrigger> Triggers = new ArrayList<>();
	
	private boolean isRedoable = false;
	private long RedoDelay;

	public String getInternalID() {
		return InternalID;
	}

	public void setInternalID(String internalID) {
		InternalID = internalID;
	}
	
	public String getQuestName() {
		return QuestName;
	}
	
	public void setQuestName(String s){
		QuestName = s;
	}

	public String getQuestOutline() {
		return QuestOutline;
	}
	
	public void setQuestOutline(String s){
		QuestOutline = s;
	}
	
	public QuestReward getQuestReward(){
		return this.reward;
	}

	public NPC getQuestNPC() {
		return QuestNPC;
	}
	
	public void setQuestNPC(NPC npc){
		QuestNPC = npc;
	}
	
	public List<QuestStage> getStages(){
		return AllStages;
	}
	
	public List<SimpleQuestObject> getAllObjects(){
		List<SimpleQuestObject> list = new ArrayList<>();
		for (QuestStage qs : AllStages){
			list.addAll(qs.getObjects());
		}
		return list;
	}
	
	
	public QuestStage getStage(int index){
		return AllStages.get(index);
	}

	public EnumMap<RequirementType, Object> getRequirements() {
		return Requirements;
	}

	public List<QuestTrigger> getTriggers() {
		return Triggers;
	}

	public void setTriggers(List<QuestTrigger> triggers) {
		Triggers = triggers;
	}
	
	public boolean hasTrigger(){
		return !Triggers.isEmpty();
	}
	
	public boolean hasRequirement(){
		return !Requirements.isEmpty();
	}
	
	public String getFailMessage(){
		return FailRequirementMessage;
	}
	
	public void setFailMessage(String s){
		FailRequirementMessage = s;
	}
	
	public boolean isRedoable(){
		return isRedoable;
	}
	
	public void setRedoable(boolean b){
		isRedoable = b;
	}
	
	public long getRedoDelay(){
		return RedoDelay;
	}
	
	public void setRedoDelay(long delay){
		RedoDelay = delay;
	}
	
	@SuppressWarnings("unchecked")
	public boolean meetRequirementWith(Player p){
		QuestPlayerData pd = QuestUtil.getData(p);
		for (RequirementType t : Requirements.keySet()){
			Object value = Requirements.get(t);
			switch (t){
			case QUEST:
				for (String q : (List<String>)value){
					if (!pd.hasFinished(QuestUtil.getQuest(q)))
						return false;
				}
				break;
			case LEVEL:
				if (!(p.getLevel() >= (Integer)value))
					return false;
				break;
			case MONEY:
				if (!(Main.economy.getBalance(p) >= (Double)value))
					return false;
				break;
			case ITEM:
				for (ItemStack i : (List<ItemStack>)value){
					if (!p.getInventory().contains(i))
						return false;
				}
				break;
			case SCOREBOARD:
				for (String s : (List<String>)value){
					String[] split;
					if (s.contains(">=")) {
						split = s.split(">=");
						if (!(Bukkit.getScoreboardManager().getMainScoreboard().getObjective(split[0])
								.getScore(p.getName()).getScore() >= Integer.parseInt(split[1])))
							return false;
					} else if (s.contains("<=")) {
						split = s.split("<=");
						if (!(Bukkit.getScoreboardManager().getMainScoreboard().getObjective(split[0])
								.getScore(p.getName()).getScore() <= Integer.parseInt(split[1])))
							return false;
					} else if (s.contains("==")) {
						split = s.split("==");
						if (!(Bukkit.getScoreboardManager().getMainScoreboard().getObjective(split[0])
								.getScore(p.getName()).getScore() == Integer.parseInt(split[1])))
							return false;
					}
				}
				break;
			case NBTTAG:
				for (String n : (List<String>)value){
					if (!((CraftPlayer)p).getHandle().P().contains(n))
						return false;
				}
				break;
			}
		}
		return true;
	}
}
