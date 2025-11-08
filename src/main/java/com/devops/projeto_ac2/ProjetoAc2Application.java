package com.devops.projeto_ac2;

import com.devops.projeto_ac2.entity.Aluno;
import com.devops.projeto_ac2.entity.AlunoRA;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@SpringBootApplication
public class ProjetoAc2Application {

	public static void main(String[] args) {
		SpringApplication.run(ProjetoAc2Application.class, args);

        AlunoRA raAna = new AlunoRA("543210");

        Aluno aluno1 = new Aluno();
        aluno1.setId(1L);
        aluno1.setNome("Ana Silva");
        aluno1.setAlunoRA(raAna);

        AlunoRA raBruno = new AlunoRA("987654");

        Aluno aluno2 = new Aluno();
        aluno2.setId(2L);
        aluno2.setNome("Bruno Mendes");
        aluno2.setAlunoRA(raBruno);

        AlunoRA raCarla = new AlunoRA("101010");

        Aluno aluno3 = new Aluno();
        aluno3.setId(3L);
        aluno3.setNome("Carla Santos");
        aluno3.setAlunoRA(raCarla);
	}

    @RequestMapping ("/test")
    @ResponseBody
    String home()
    {
        return "Hello World";
    }
}
