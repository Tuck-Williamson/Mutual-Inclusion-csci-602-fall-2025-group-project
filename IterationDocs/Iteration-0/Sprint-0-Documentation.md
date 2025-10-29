# Team Meeting - Sprint 0 Planning - October 29, 2025 @ 17:00

## Quick recap

The team discussed technical challenges and project management approaches, including
database replication, task organization, and acceptance criteria implementation. They
focused on setting up a database and implementing test-driven development, with specific
assignments made for creating endpoints and list functionalities. The team concluded by
addressing PR review processes, database initialization, and build issues, while agreeing
to meet again the following day.

## Next steps

- Dan: Create dB init with in-memory database by tomorrow before bed
- Dan: Create feature for creating a new list
- Dan: Send out pings tomorrow for PR review
- Christian: Create Hello World endpoint
- Christian: Create status endpoints
- Erin: Edit a list feature
- Erin: View a list feature
- Tuck: Strip the repo of cruft/example code
- Tuck: Delete a list feature
- Tuck: Move any issues that appear in wrong project to correct project as needed
- Tuck: Handle documentation PRs and merge directly
- All team members: Review PRs from other developers
- All team members: Add any new ideas or bugs to backlog as they arise

## Summary

### Project Management Strategy

The team discussed their approach to managing the project, deciding not to assign points
to tasks due to the short timeline and limited team size. They agreed to focus on
completing the minimum viable tasks and potentially adding stretch goals later. Dan
emphasized the importance of being mindful of burnout, especially given the 5-week
timeline and everyone's other commitments. Tuck explained a technical issue with adding
tasks to the correct GitHub repository and clarified that he would handle transferring any
incorrectly added items.

### Streamlined Acceptance Criteria Process

The team discussed the process for defining and implementing acceptance criteria (ACs) for
their project. Dan emphasized the importance of keeping the process simple, suggesting
that a README and verbal agreement would suffice, rather than creating a complex
documentation system. The team agreed that PRs should be issued early and often, with a
review process to ensure code understandability and functionality. Dan proposed that the
definition of done should include meeting all ACs, PR merging, and passing regression
testing.

### Database Setup and Development Planning

The team discussed setting up a database and implementing test-driven development. Dan
emphasized the importance of creating Flyway scripts early to make future database changes
easier. They agreed to start with a simple "Hello World" endpoint before adding more
complex functionality. Erin McCall was assigned to work on the "Hello World" feature. The
team also addressed the need to review the repository's default settings and discussed
merge queue functionality to handle potential conflicts.

### Server Setup and Database Integration

The team discussed implementing a basic server setup and database integration. Dan
suggested stripping down the codebase to create a minimal "Hello World" endpoint while
keeping the data layer intact. They agreed to prioritize creating a new list feature, with
Tuck taking responsibility for the database initialization story. The team decided to
postpone user authentication features, with Dan recommending OAuth as a secure alternative
when that time comes.

### Project Progress and Task Dependencies

The team discussed linking and cloning tasks, with Dan explaining that certain work cannot
proceed until another task is completed but can still be added to Ready. They explored
options for demonstrating project progress, including using curl statements, Postman, or
GitHub, with Erin suggesting GitHub might be useful for consolidating information. Dan
outlined three concurrent tasks that need to be completed before more parallel work can
begin, focusing on database integration as a key component.

### Sprint Planning: Core Functionalities

The team discussed sprint planning and agreed on four main items to focus on: stripping
cruft from the repo, creating a Hello World endpoint, implementing status endpoints as a
spike for testing, and developing an account endpoint. They decided to keep the sprint
scope constrained to these core functionalities, aiming to have usable results by the end
of the sprint. Dan explained that spikes are used for proving concepts, and the team
agreed this approach would help them determine viable options before committing to full
implementation.

### Project Task Assignments and Endpoints

The team discussed and assigned tasks for their project, focusing on creating a database,
endpoints, and list functionalities. Christian agreed to work on creating the "Hello
World" endpoint and the status endpoint, while Erin expressed her preference for working
on list-related tasks like edit and delete functions, despite them being dependent on
other team members' work. Dan took on the database initialization and creating a new list
feature, emphasizing the importance of collaboration and flexibility in adding or
addressing bugs and new features as they arise.

### PR Review Process Updates

The team discussed PR review processes and agreed that each PR should be reviewed by a
different team member before merging to master. Dan announced he would submit a PR in the
coming days, and the team aligned on using an in-memory database for initial development.
Tuck raised concerns about protection rules, and Dan advised removing the approval
required setting to allow for easier PR updates and history rewriting. The team also
discussed the possibility of creating a development branch and using master as a tagged
release branch in the future.

### Database Initialization and Build Issues

The team discussed database initialization and build issues. Dan explained they would use
an in-memory database for GitHub checks, which would be updated with each build. Christian
reported build failures and received help from Dan to resolve the issues. Tuck confirmed
that the Scrum Master would handle documentation, and Dan agreed to approve PRs for
documentation without review. The team agreed to meet again the next day at 5 PM.
