#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Analisador de Cobertura de Release 1 - Todos os RF
"""

import os
import re
from pathlib import Path
from collections import defaultdict

class ReleaseOneAnalyzer:
    def __init__(self, project_dir):
        self.project_dir = Path(project_dir)
        self.test_dir = self.project_dir / "src" / "test" / "java" / "classroompb" / "service"

        # Todos os RF da Release 1
        self.rfs = {
            'RF01': 'Cadastro de Usuários (Aluno, Professor, Coordenador, Administrador)',
            'RF02': 'Login de Usuários',
            'RF03': 'Menu de Perfil do Usuário',
            'RF04': 'Rejeição de Cadastro Duplicado',
            'RF05': 'Cadastro de Cursos',
            'RF06': 'Cadastro de Disciplinas',
            'RF08': 'Cadastro de Períodos Letivos',
            'RF09': 'Gerenciamento de Períodos Letivos',
            'RF10': 'Oferta de Turmas',
            'RF11': 'Características de Turma (Professor, Vagas, Horário, Sala)',
            'RF12': 'Validação de Choque de Horário do Professor',
            'RF13': 'Validação de Turma sem Professor',
            'RF14': 'Alterar/Cancelar Turma',
        }

    def find_test_files(self):
        """Encontra todos os arquivos de teste"""
        tests = {}

        if self.test_dir.exists():
            for test_file in self.test_dir.glob("*Test.java"):
                with open(test_file, 'r', encoding='utf-8', errors='ignore') as f:
                    content = f.read()

                # Procura por qual RF este teste cobre
                for rf_code in self.rfs.keys():
                    if rf_code in test_file.name:
                        if rf_code not in tests:
                            tests[rf_code] = []

                        # Conta testes
                        test_methods = re.findall(r'@Test.*?void (\w+)\(\)', content, re.DOTALL)

                        tests[rf_code].append({
                            'file': test_file.name,
                            'count': len(test_methods),
                            'methods': test_methods
                        })
                        break

        return tests

    def count_test_methods(self, file_path):
        """Conta métodos de teste em um arquivo"""
        try:
            with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                content = f.read()
            methods = re.findall(r'@Test.*?void (\w+)\(\)', content, re.DOTALL)
            return len(methods), methods
        except:
            return 0, []

    def generate_report(self):
        """Gera relatório de cobertura de RF"""
        tests = self.find_test_files()

        print("\n" + "="*90)
        print("🎯 ANÁLISE DE COBERTURA DE RELEASE 1 - TODOS OS RF")
        print("="*90 + "\n")

        # Resumo por RF
        print("📋 COBERTURA POR REQUISITO FUNCIONAL:\n")

        total_tests = 0
        rfs_covered = 0
        rfs_missing = []

        for rf_code in sorted(self.rfs.keys()):
            rf_name = self.rfs[rf_code]

            if rf_code in tests:
                test_data = tests[rf_code]
                total = sum(t['count'] for t in test_data)
                total_tests += total
                rfs_covered += 1

                status = "✅"
                file_info = " | ".join([f"{t['file']}({t['count']})" for t in test_data])

                print(f"{status} {rf_code}: {rf_name}")
                print(f"   Arquivos: {file_info}")
                print(f"   Total de testes: {total}")
                print()
            else:
                status = "⚠️"
                rfs_missing.append((rf_code, rf_name))

                print(f"{status} {rf_code}: {rf_name}")
                print(f"   ❌ SEM TESTES")
                print()

        print("="*90 + "\n")

        # Resumo
        print("📊 RESUMO EXECUTIVO:\n")
        print(f"Total de RF na Release 1: {len(self.rfs)}")
        print(f"RF com testes: {rfs_covered}/{len(self.rfs)} ({rfs_covered*100//len(self.rfs)}%)")
        print(f"Total de testes implementados: {total_tests}+")
        print()

        if rfs_missing:
            print("⚠️  RF SEM COBERTURA DE TESTES:\n")
            for rf_code, rf_name in rfs_missing:
                print(f"   • {rf_code}: {rf_name}")
            print()
        else:
            print("✅ TODOS OS RF COM COBERTURA DE TESTES!")
            print()

        print("="*90 + "\n")

        # Requisitos de Release 1
        print("🎯 REQUISITOS PARA RELEASE 1:\n")
        print("✓ Cobertura global: ≥ 80%")
        print("✓ Pacote model: ≥ 80%")
        print("✓ Pacote service: ≥ 80%")
        print("✓ Todos os RF (RF01-RF14) com testes")
        print("✓ Todas as RN (RN01-RN12) implementadas")
        print()

        # Teste por RF
        print("="*90)
        print("\n📈 DISTRIBUIÇÃO DE TESTES POR RF:\n")

        for rf_code in sorted(self.rfs.keys()):
            if rf_code in tests:
                test_data = tests[rf_code]
                total = sum(t['count'] for t in test_data)
                bar_length = total // 2 if total > 0 else 1
                bar = "█" * bar_length
                print(f"{rf_code:5} │ {bar:30} │ {total:2} testes")

        print("\n" + "="*90 + "\n")

if __name__ == "__main__":
    import sys

    project_dir = sys.argv[1] if len(sys.argv) > 1 else "."
    analyzer = ReleaseOneAnalyzer(project_dir)

    analyzer.generate_report()



