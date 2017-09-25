import subprocess

projects = ["<project name>"]

directory = "/path/to/sunil/projects/dp"
repoURLPrefix = "<ssh gerrit url to repo with port>/"

for project in projects:
    repoURL = repoURLPrefix + project
    print("Processing RepoURL: " + repoURL)
    command = "cd " + directory + "; git clone " + repoURL

    print(command)
    subprocess.call(command, shell=True)
