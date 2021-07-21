/*
 * (c) 2015 Abil'I.T. http://abilit.eu/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.scada_lts.web.mvc.controller;

import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scada_lts.errors.ErrorCode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import br.org.scadabr.db.dao.ScriptDao;
import br.org.scadabr.rt.scripting.ScriptRT;
import br.org.scadabr.vo.scripting.ScriptVO;

import com.serotonin.mango.Common;
import com.serotonin.mango.vo.User;

import static com.serotonin.mango.util.LoggingScriptUtils.infoErrorExecutionScript;

/**
 * Controller for script.
 * 
 * @author grzegorz bylica Abil'I.T. development team, sdt@abilit.eu person
 *         supporting and coreecting translation Jerzy Piejko
 */
@Controller
public class ScriptController {

	private static final Log LOG = LogFactory.getLog(ScriptController.class);

	@RequestMapping(value = "/script/execute/{xid}", method = RequestMethod.POST)
	public @ResponseBody String setLocale(@PathVariable("xid") String xid,
			HttpServletRequest request, HttpServletResponse response) {
		if (LOG.isTraceEnabled()) {
			LOG.trace("/script/{xid} xid:" + xid);
		}
		// TODO ta metoda powinna przyjmowac jako argument tresc skryptu z
		// odpowiednimi argumentami nie powinno to isc przez baze danych !!!.
		User user = Common.getUser(request);
		String result = "";
		if (user == null) {
			response.setStatus(ErrorCode.USER_NOT_LOGGED);
			result = "";
		} else {
			ScriptVO<?> script = new ScriptDao().getScript(xid);
			try {
				if (script != null) {
					ScriptRT rt = script.createScriptRT();
					rt.execute();
				}
				response.setStatus(ErrorCode.NO_ERROR);
			} catch (ScriptException e) {
				response.setStatus(ErrorCode.SCRIPT_NOT_EXECUTED);
				result = "" + e.getMessage();
				LOG.error(infoErrorExecutionScript(e, script));
			} catch (Exception e) {
                LOG.error(infoErrorExecutionScript(e, script));
				throw e;
			}
		}

		return result;

	}

}
