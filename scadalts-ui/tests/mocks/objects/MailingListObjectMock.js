export let mlMock = {
	id: 1,
	xid: 'ML0001',
	name: 'Example MailingList',
	entries: [
		{
			userId: 1,
			user: {
				id: 1,
				username: 'admin',
				email: 'admin@yourMangoDomain.com',
				phone: '',
				admin: true,
				disabled: false,
				homeUrl: '',
				lastLogin: 1614587974686,
				firstLogin: false,
			},
			recipientType: 2,
			referenceAddress: null,
			referenceId: 1,
		},
		{
			userId: 3,
			user: {
				id: 3,
				username: 'tester',
				email: 'tester@mail.com',
				phone: '123123131',
				admin: true,
				disabled: false,
				homeUrl: null,
				lastLogin: 0,
				firstLogin: true,
			},
			recipientType: 2,
			referenceAddress: null,
			referenceId: 3,
		},
		{
			address: 'mail@mail.com',
			recipientType: 3,
			referenceAddress: 'mail@mail.com',
			referenceId: 0,
		},
	],
	cronPattern: '1 */15 * * * ?',
	collectInactiveEmails: false,
	dailyLimitSentEmailsNumber: 0,
	dailyLimitSentEmails: false,
	inactiveIntervals: [0,1,2,3],
};
export default mlMock