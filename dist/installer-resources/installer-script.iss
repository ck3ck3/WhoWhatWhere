; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

#define MyAppName "Who What Where"
#define MyAppVersion "1.00"
#define MyAppPublisher "ck3ck3"
#define MyAppURL "http://ck3ck3.github.io/WhoWhatWhere"
#define MyAppExeName "WhoWhatWhere.exe"
#define JNativeHookDLL "JNativeHook.dll"
#define JNetPcapDLL "jnetpcap.dll"
#define WinPcapInstallerFilename "WinPcap_4_1_3.exe"
#define JRE_DIRNAME "jre1.8.0_121"
#define schtask_xml "www-task.xml"
#define schtask_name "Who What Where launcher {app}"
#define schtask_updater "TaskXMLUpdater.jar"
#define command_placeholder "command_placeholder"

[Setup]
; NOTE: The value of AppId uniquely identifies this application.
; Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId={{11D41849-86CD-4964-80A2-A8271D4A3F44}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={pf}\{#MyAppName}
DefaultGroupName={#MyAppName}
AllowNoIcons=yes
LicenseFile=..\..\licenses\LICENSE-WhoWhatWhere
OutputDir=..\Installer
OutputBaseFilename=WhoWhatWhere-{#MyAppVersion}-installer
Compression=lzma
SolidCompression=yes
UninstallDisplayIcon={app}\{#MyAppExeName}

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"

[Files]
Source: {#schtask_xml}; DestDir: "{app}"; Flags: dontcopy
Source: {#schtask_updater}; DestDir: "{app}"; Flags: dontcopy
Source: {#WinPcapInstallerFilename}; DestDir: "{app}"; Flags: dontcopy
Source: "..\exe\WhoWhatWhere.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: {#JNativeHookDLL}; DestDir: "{app}"; Flags: ignoreversion
Source: {#JNetPcapDLL}; DestDir: "{app}"; Flags: ignoreversion
Source: "{#JRE_DIRNAME}\*"; DestDir: "{app}\{#JRE_DIRNAME}"; Flags: ignoreversion recursesubdirs
Source: "..\..\licenses\*"; DestDir: "{app}\licenses"; Flags: ignoreversion recursesubdirs
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{sys}\schtasks.exe"; Parameters: "/run /tn ""{#schtask_name}"""; IconFilename: "{app}\{#MyAppExeName}"; Flags: runminimized
Name: "{group}\{cm:ProgramOnTheWeb,{#MyAppName}}"; Filename: "{#MyAppURL}"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"
Name: "{commondesktop}\{#MyAppName}"; Filename: "{sys}\schtasks.exe"; Parameters: "/run /tn ""{#schtask_name}"""; IconFilename: "{app}\{#MyAppExeName}"; Flags: runminimized; Tasks: desktopicon

[Code]
var
  prereqPage: TOutputMsgWizardPage;
  progressBarPage: TOutputProgressWizardPage;
  
function IsWinPcapInstalled(): Boolean;
begin
  Result := FileExists(ExpandConstant('{sys}\wpcap.dll'));
end;

function GetInstalledVersion: String;
var
  RegKey: String;
begin
  Result := '';
  RegKey := ExpandConstant('Software\Microsoft\Windows\CurrentVersion\Uninstall\{#emit SetupSetting("AppId")}_is1');
  if not RegQueryStringValue(HKEY_LOCAL_MACHINE, RegKey, 'DisplayVersion', Result) then
    RegQueryStringValue(HKEY_CURRENT_USER, RegKey, 'UninstallString', Result);
end;

function InitializeSetup: Boolean;
var
  installedVersion, msg : String;
  compareResult, userResponse : Integer;
begin
  installedVersion := GetInstalledVersion;
  if installedVersion <> '' then
  begin
    compareResult := CompareStr(installedVersion, RemoveQuotes('{#MyAppVersion}'));
    if (compareResult < 0) then //this version is newer than the installed version, meaning an upgrade
    begin
      msg := 'Do you want to upgrade {#MyAppName} version ' + installedVersion + ' to {#MyAppVersion}?';
    end
    else
      if (compareResult = 0) then //repair?
      begin
        msg := '{#MyAppName} version {#MyAppVersion} is already installed. Do you want to repair (overwrite) this installation with a new one? Your user files and settings will be safe.';
      end
      else //this version is older than the installed version, downgrade?
        msg := 'This installer will downgrade {#MyAppName}. Version ' + installedVersion + ' is currently installed, and this installer will overwrite it with version {#MyAppVersion}' + #13#10 + 'It is not guaranteed that your user files and settings are compatible with an older version. Proceed with the downgrade anyway?';

    Result := MsgBox(msg, mbConfirmation, MB_YESNO or MB_DEFBUTTON2) = IDYES
  end
  else
    Result := True;
end;

procedure InitializeWizard();
begin
  if (not IsWinPcapInstalled) then
  begin
    prereqPage := CreateOutputMsgPage(wpLicense, 'Required Prerequisites', 'WinPcap must be installed', 'WinPcap must be installed in order to run {#MyAppName}. WinPcap is a tool that enables monitoring network traffic (see http://winpcap.org for more details).'#13#10#13#10'Please click "Next" to launch WinPcap''s installer. {#MyAppName}''s installation will resume once WinPcap is installed.');
    progressBarPage := CreateOutputProgressPage('Installing Prerequisites', 'Installing WinPcap');
  end
end;

function NextButtonClick(CurPageID: Integer): Boolean;
var
  execSuccess, installSuccess : Boolean;
  resultCode : Integer;
begin
  if (not IsWinPcapInstalled and (CurPageID = prereqPage.ID)) then
  begin
    progressBarPage.setText('Installing WinPcap', '');
    progressBarPage.ProgressBar.Style := npbstMarquee;
    progressBarPage.Show;
    progressBarPage.SetProgress(1, 2);
    try
      ExtractTemporaryFile('{#WinPcapInstallerFilename}');
      execSuccess := Exec(ExpandConstant('{tmp}\{#WinPcapInstallerFilename}'), '', '', SW_SHOW, ewWaitUntilTerminated, resultCode);
    finally
      progressBarPage.Hide;
    end;
    installSuccess := execSuccess and (resultCode = 0);
    if (not installSuccess) then
    begin
      MsgBox('Installation of WinPcap failed.'#13#10'You can try installing it again by clicking "Next" on the installer, or exit the installer and try to install WinPcap manually by visiting http://www.winpcap.org/', mbError, MB_OK);
    end;
    Result := installSuccess;
  end
  else //any other page
  begin
    Result := True;
  end
end;

procedure CurStepChanged(CurStep: TSetupStep);
var
  resultCode : Integer;
  command, params: String;
begin
  if (CurStep = ssPostInstall) then
  begin
    ExtractTemporaryFile('{#schtask_xml}');
    ExtractTemporaryFile('{#schtask_updater}');
    
    command := ExpandConstant('{app}\{#JRE_DIRNAME}\bin\java.exe');
    params := ExpandConstant('-jar "{tmp}\{#schtask_updater}" "{tmp}\{#schtask_xml}" {#command_placeholder} "\"{app}\{#MyAppExeName}\"" "{tmp}\updated_{#schtask_xml}"');
    Exec(command, params, '', SW_HIDE, ewWaitUntilTerminated, resultCode);
log('running ' + command + ' ' + params);
    command := ExpandConstant('{sys}\schtasks.exe');
    params := ExpandConstant('/create /tn "{#schtask_name}" /xml "{tmp}\updated_{#schtask_xml}"');
    Exec(command, params, '', SW_HIDE, ewWaitUntilTerminated, resultCode);
log('running ' + command + ' ' + params);
  end;
end;

procedure CurUninstallStepChanged(CurUninstallStep: TUninstallStep);
var
  response: Integer;
begin
  if (CurUninstallStep = usPostUninstall) then
  begin
    response := MsgBox('Do you want to also delete {#MyAppName}''s user files? These files include configuration files, IP Notes, Watchdog rule lists, Quick Ping history etc.', mbConfirmation, MB_YESNO or MB_DEFBUTTON2);
    if (response = IDYES) then
    begin
      DelTree(ExpandConstant('{userappdata}\{#MyAppName}'), True, True, True);
    end;
  end;
end;

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent runascurrentuser

[UninstallRun]
Filename: "{sys}\schtasks.exe"; Parameters: "/delete /tn ""{#schtask_name}"" /f"

[UninstallDelete]
Type: files; Name: "{app}\{#MyAppName}.log*"

[Registry]
Root: HKLM; Subkey: "Software\Microsoft\Windows\CurrentVersion\Run"; ValueName: {#MyAppName}; Flags: uninsdeletevalue
Root: HKCU; Subkey: "Software\Microsoft\Windows\CurrentVersion\Run"; ValueName: {#MyAppName}; Flags: uninsdeletevalue