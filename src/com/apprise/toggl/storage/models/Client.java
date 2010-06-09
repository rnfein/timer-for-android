package com.apprise.toggl.storage.models;

public class Client extends Model {
  
  public String currency;
  public Workspace workspace;
  public float hourly_rate;  
  public String name;
  
  public Client() {}
  
  public Client(long _id, long remoteId, String name, Workspace workspace, float hourlyRate,
      String currency) {
    this._id = _id;
    this.id = remoteId;
    this.name = name;
    this.workspace = workspace;
    this.hourly_rate = hourlyRate;
    this.currency = currency;
  }
  
  public String toString() {
    Long workspace_remote_id = null;
    
    if (workspace != null)
      workspace_remote_id = new Long(workspace.id);
    
    return "workspace_id: " + workspace_remote_id
      + ", remote_id: " + id
      + ", name: " + name
      + ", hourly_rate: " + hourly_rate
      + ", currency: " + currency;
  }

  public boolean identicalTo(Model other) {
    Client otherClient = (Client) other;
    
    return this.toString().equals(otherClient.toString());
  }
    
  public void updateAttributes(Model other) {
    Client otherClient = (Client) other;
    workspace = otherClient.workspace;
    name = otherClient.name;
    hourly_rate = otherClient.hourly_rate;
    currency = otherClient.currency;
  }  

}
