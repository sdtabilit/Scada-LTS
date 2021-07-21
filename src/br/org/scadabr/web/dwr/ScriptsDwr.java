package br.org.scadabr.web.dwr;

import java.util.List;

import br.org.scadabr.db.dao.ScriptDao;
import br.org.scadabr.rt.scripting.ScriptRT;
import br.org.scadabr.vo.scripting.ContextualizedScriptVO;
import br.org.scadabr.vo.scripting.ScriptVO;

import com.serotonin.db.IntValuePair;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.vo.DataPointExtendedNameComparator;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.web.dwr.BaseDwr;
import com.serotonin.web.dwr.DwrResponseI18n;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static com.serotonin.mango.util.LoggingScriptUtils.infoErrorExecutionScript;

public class ScriptsDwr extends BaseDwr {

	private static final Log LOG = LogFactory.getLog(ScriptsDwr.class);


	public List<DataPointVO> getPoints() {
		List<DataPointVO> allPoints = new DataPointDao().getDataPoints(
				DataPointExtendedNameComparator.instance, false);
		return allPoints;
	}

	public List<ScriptVO<?>> getScripts() {
		List<ScriptVO<?>> scripts = new ScriptDao().getScripts();
		return scripts;
	}

	public ScriptVO<?> getScript(int id) {
		if (id == Common.NEW_ID) {
			ContextualizedScriptVO vo = new ContextualizedScriptVO();
			vo.setXid(new ScriptDao().generateUniqueXid());
			return vo;
		}

		return new ScriptDao().getScript(id);
	}

	public DwrResponseI18n saveScript(int id, String xid, String name,
			String script, List<IntValuePair> pointsOnContext,
			List<IntValuePair> objectsOnContext) {

		ContextualizedScriptVO vo = new ContextualizedScriptVO();
		vo.setId(id);
		vo.setXid(xid);
		vo.setName(name);
		vo.setScript(script);
		vo.setPointsOnContext(pointsOnContext);
		vo.setObjectsOnContext(objectsOnContext);
		vo.setUserId(Common.getUser().getId());

		DwrResponseI18n response = new DwrResponseI18n();

		vo.validate(response);

		if (!response.getHasMessages())
			new ScriptDao().saveScript(vo);

		response.addData("seId", vo.getId());
		return response;
	}

	public void deleteScript(int scriptId) {
		new ScriptDao().deleteScript(scriptId);
	}

	public boolean executeScript(int scriptId) {
		ScriptVO<?> script = new ScriptDao().getScript(scriptId);

		try {
			if (script != null) {
				ScriptRT rt = script.createScriptRT();
				rt.execute();
				return true;
			}
		} catch (Exception e) {
			LOG.error(infoErrorExecutionScript(e,script), e);
		}

		return false;
	}
}
